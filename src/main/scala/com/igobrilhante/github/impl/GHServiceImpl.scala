package com.igobrilhante.github.impl

import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import com.igobrilhante.github.api.GHService
import com.igobrilhante.github.commons.{CacheHeader, CacheHelper, Logging}
import com.igobrilhante.github.models.{GHContributor, GHOrganization, GHRepository}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scalacache.ehcache.EhcacheCache
import scalacache.modes.scalaFuture._
import scalacache._

class GHServiceImpl()(implicit val system: ActorSystem[_], val ec: ExecutionContext) extends GHService with Logging {

  private type ContributorRepo = (GHContributor, GHRepository)
  private val mat = Materializer.matFromSystem
  private val BaseApi = "https://api.github.com"
  private val http = Http(system)
  private val MaxReposPerPage = 100
  private val MaxContributorsPerPage = 100
  private val config = system.settings.config
  private val githubConfig = config.getConfig("github")
  private val GitHubToken = githubConfig.getString("token")
  private val GitHubCredentials = OAuth2BearerToken(GitHubToken)
  private val t = Http().cachedHostConnectionPoolHttps("api.github.com")

  val underlying = CacheHelper.getContributorsCache
  implicit val flags = Flags()
  implicit val cache: Cache[List[GHContributor]] = EhcacheCache(underlying)

  override def getOrganization(organizationId: String): Future[GHOrganization] = {
    val uri = s"$BaseApi/orgs/$organizationId"
    for {
      response <- request(uri)
      list <- {
        Unmarshal(response).to[GHOrganization]
      }
    } yield list
  }

  override def getRepositories(organizationId: String, page: Int = 1): Future[List[GHRepository]] = {
    logger.debug("getRepositories for org {} and page {}", organizationId, page)
    val uri = s"$BaseApi/orgs/$organizationId/repos?page=$page&per_page=$MaxReposPerPage"
    for {
      response <- request(uri)
      list <- Unmarshal(response).to[Option[List[GHRepository]]].map(optionToEmptyList)
    } yield list

  }

  override def getContributors(organizationId: String, repositoryId: String, page: Int): Future[List[GHContributor]] = {
    logger.debug("getContributors for org {}, repo {} and page {}", organizationId, repositoryId, page)
    val uri =
      s"$BaseApi/repos/$organizationId/$repositoryId/contributors?per_page$MaxContributorsPerPage&page=$page"

    cachingF[Future, List[GHContributor]](uri)(Some(10.hours)) {
      for {
        response <- request(uri)
        list <- Unmarshal(response).to[Option[List[GHContributor]]].map(optionToEmptyList)
      } yield list
    }
  }

  override def getAllContributors(organizationId: String, repositoryId: String): Future[List[GHContributor]] = {
    logger.debug("getAllContributors for org {}, repo {}", organizationId, repositoryId)
    def get(page: Int) = getContributors(organizationId, repositoryId, page)
    paginate(1, MaxContributorsPerPage)(get)
  }

  def getRankedContributors(organizationId: String): Future[List[GHContributor]] = {
    computeRankingWithStreaming(organizationId)
      .map(_.take(20))
  }

  def getAllRepositories(organizationId: String): Future[List[GHRepository]] = {
    logger.debug("getAllRepositories for org {}", organizationId)
    getAllRepositoriesFoldLeftStrategy(organizationId)
  }

  private def optionToEmptyList[A](optionList: Option[List[A]]): List[A] = optionList.getOrElse(List.empty[A])

  private def computeRankingWithStreaming(organizationId: String) = {

    def getContributorsWithRepo(repository: GHRepository) = {
      getAllContributors(organizationId, repository.name).map(c => (repository, c))
    }

    def computeContributorsRank(list: List[ContributorRepo]) = {
      list
        .groupBy(_._1.login)
        .map {
          case (_, contributorRepositories) =>
            val totalContributions = contributorRepositories.map(_._1.contributions).sum
            val contributor = contributorRepositories.head._1
            contributor.copy(contributions = totalContributions)
        }
        .toList
        .sortBy(_.contributions)(Ordering[Int].reverse)
    }

    def source(pages: Int) = Source.fromIterator(() => (1 to pages).iterator)

    def sink =
      Sink.fold[List[ContributorRepo], ContributorRepo](List.empty[ContributorRepo]) {
        case (result, tuple) => result :+ tuple
      }

    val futureSource = for {
      org <- getOrganization(organizationId)
      pages = getNumberOfPages(org.total, MaxReposPerPage)
      repoPagesSource = source(pages)
    } yield repoPagesSource

    val futureList = Source
      .futureSource(futureSource)
      .mapAsyncUnordered(2) { getRepositories(organizationId, _) }
      .mapConcat(identity)
      .throttle(50, 1 second)
      .mapAsync(4)(getContributorsWithRepo)
      .map { case (repo, contributors) => contributors.map((_, repo)) }
      .mapConcat(identity)
      .toMat(sink)(Keep.right)
      .run()

    for {
      list <- futureList
      rankedList = computeContributorsRank(list)
    } yield rankedList
  }

  private def computeRankingNaive(organizationId: String) = {
    logger.debug("getRankedContributors for org {}", organizationId)
    def rankContributors(list: List[(GHRepository, List[GHContributor])]) = {
      logger.debug("rankContributors for org {}", organizationId)
      list
        .flatMap { case (repo, contributors) => contributors.map((_, repo)) }
        .groupBy(_._1.login)
        .map {
          case (_, contributorRepositories) =>
            val totalContributions = contributorRepositories.map(_._1.contributions).sum
            val contributor = contributorRepositories.head._1
            contributor.copy(contributions = totalContributions)
        }
        .toList
        .sortBy(_.contributions)(Ordering[Int].reverse)

    }

    for {
      allRepositories <- getAllRepositories(organizationId)
      reposAndContributors <-
        Future.sequence(allRepositories.map(repo => getAllContributors(organizationId, repo.name).map(c => (repo, c))))
      result = rankContributors(reposAndContributors)
    } yield result

  }

  private def getAllRepositoriesFoldLeftStrategy(organizationId: String): Future[List[GHRepository]] = {

    def getReposForPages(pages: Int) = {
      (1 to pages)
        .map(page => getRepositories(organizationId, page))
        .foldLeft(Future.successful(List.empty[GHRepository])) {
          case (result, future) =>
            for {
              currentList <- result
              next <- future
            } yield currentList ++ next
        }
    }

    for {
      org <- getOrganization(organizationId)
      total = org.total
      pages = getNumberOfPages(total, MaxReposPerPage)
      result <- getReposForPages(pages)
    } yield result
  }

  private def getNumberOfPages(total: Int, maxPerPage: Int): Int = math.ceil(total / maxPerPage.toDouble).toInt

  private def getAllRepositoriesRecursiveStrategy(organizationId: String): Future[List[GHRepository]] = {
    def getRepos(page: Int): Future[List[GHRepository]] = getRepositories(organizationId, page)
    paginate(1, MaxReposPerPage)(getRepos)
  }

  private def paginate[A](page: Int, maxPerPage: Int)(fn: Int => Future[List[A]]): Future[List[A]] = {
    for {
      repos <- fn(page)
      result <- {
        if (repos.length < maxPerPage) Future.successful(repos)
        else paginate(page + 1, maxPerPage)(fn).map(_ ++ repos)
      }
    } yield result
  }

  private def request(uri: String) = {

    def internalCache(uri: String): Future[Option[CacheHeader]] = {
      Future.successful(None)
    }

    // TODO consider headers ETAG and Last-Modified to void consuming limits of Github API
    def handleCacheHeaders(optCache: Option[CacheHeader]): Seq[HttpHeader] = {
      optCache match {
        case Some(cacheHeader) =>
          cacheHeader.etag.fold(Seq.empty[HttpHeader]) { etag => Seq(headers.ETag(etag)) }
          cacheHeader.lastModified.fold(Seq.empty[HttpHeader]) { last =>
            Seq(headers.`If-Modified-Since`(DateTime(last)))
          }
        case None =>
          // simplesmente envia a requisição
          Seq()
      }
    }

    def makeRequest(httpRequest: HttpRequest) =
      http
        .singleRequest(httpRequest)
        .map(interceptResponseFor(uri))

    internalCache(uri)
      .map { handleCacheHeaders }
      .map { cacheHeaders =>
        HttpRequest(uri = uri).withHeaders(
          headers.Authorization(GitHubCredentials),
          cacheHeaders: _*
        )
      }
      .flatMap(makeRequest)

  }

//  private def handleNotModified(response: HttpResponse)

  private def interceptResponseFor(uri: String)(response: HttpResponse): HttpResponse = {

    response.status match {
      case StatusCodes.Success(status) =>
      // Persist the ETag and Last-Modified headers
      case _ =>
        logFailedStatus(uri, response)
    }

    response
  }

  private def logFailedStatus(uri: String, response: HttpResponse): Unit = {
    logger.error("Failed to request {}", uri)
    logger.error(response.toString())
    logger.error(response.headers.mkString("\n"))
  }

//  private def request2[A](uri: String)(implicit um: Unmarshaller[HttpResponse, A]) =
//    http
//      .singleRequest(HttpRequest(uri = uri))
//      .flatMap(response => Unmarshal(response).to[A](um, ec, mat))

}
