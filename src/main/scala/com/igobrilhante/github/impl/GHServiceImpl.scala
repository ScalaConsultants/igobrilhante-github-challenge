package com.igobrilhante.github.impl

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.{Http, HttpExt}

import cats.data._
import cats.implicits._
import com.igobrilhante.github.api.GHService
import com.igobrilhante.github.commons.{CacheHelper, Logging}
import com.igobrilhante.github.models._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scalacache._
import scalacache.ehcache.EhcacheCache

class GHServiceImpl()(implicit val system: ActorSystem[_], val ec: ExecutionContext)
  extends GHService
    with GitHubAPIHandlers
    with Logging {

  private type ContributorRepo = (GHContributor, GHRepository)
  private val underlying = CacheHelper.getContributorsCache
  private val BaseApi = "https://api.github.com"

  private val MaxReposPerPage = 100
  private val MaxContributorsPerPage = 100

  val http: HttpExt = Http(system)

  implicit val flags: Flags = Flags()
  implicit val cache: Cache[List[GHContributor]] = EhcacheCache(underlying)

  override def getOrganization(organizationId: String): Future[Option[GHOrganization]] = {
    logger.info("getOrganization for org {} ", organizationId)
    val uri = s"$BaseApi/orgs/$organizationId"
    for {
      response <- request(uri)
      list <- {
        response.status match {
          case StatusCodes.NotFound => Future.failed(OrganizationNotFound(organizationId))
          case StatusCodes.NoContent => Future.failed(OrganizationNotFound(organizationId))
          case _ => Unmarshal(response).to[Option[GHOrganization]]
        }
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

  override def getContributors(
                                organizationId: String,
                                repositoryId: String,
                                page: Int
                              ): Future[List[GHContributor]] = {
    logger.debug("getContributors for org {}, repo {} and page {}", organizationId, repositoryId, page)
    val uri =
      s"$BaseApi/repos/$organizationId/$repositoryId/contributors?per_page$MaxContributorsPerPage&page=$page"

    //    cachingF[Future, List[GHContributor]](uri)(Some(10.hours)) {
    for {
      response <- request(uri)
      list <- Unmarshal(response).to[Option[List[GHContributor]]].map(optionToEmptyList)
    } yield list

    //    }
  }

  override def getAllContributors(
                                   organizationId: String,
                                   repositoryId: String
                                 ): Future[List[GHContributor]] = {
    logger.debug("getAllContributors for org {}, repo {}", organizationId, repositoryId)

    def get(page: Int) = getContributors(organizationId, repositoryId, page)

    paginate(1, MaxContributorsPerPage)(get)
  }

  def getAllRepositories(organizationId: String): Future[List[GHRepository]] = {
    logger.debug("getAllRepositories for org {}", organizationId)
    getAllRepositoriesFoldLeftStrategy(organizationId)
  }

  private def optionToEmptyList[A](optionList: Option[List[A]]): List[A] = optionList.getOrElse(List.empty[A])

  private def getAllRepositoriesFoldLeftStrategy(organizationId: String): Future[List[GHRepository]] = {

    def getReposForPages(pages: Int) = {
      (1 to pages)
        .map(page => getRepositories(organizationId, page))
        .foldLeft(Future.successful(List.empty[GHRepository])) { case (result, future) =>
          for {
            currentList <- result
            next <- future
          } yield currentList ++ next
        }
    }

    val res = for {
      org <- OptionT(getOrganization(organizationId))
      total = org.total
      pages = getNumberOfPages(total, MaxReposPerPage)
      result <- OptionT.liftF(getReposForPages(pages))
    } yield result

    res.value.map(_.getOrElse(List.empty))
  }

  private def getNumberOfPages(total: Int, maxPerPage: Int): Int = math.ceil(total / maxPerPage.toDouble).toInt

  private def getAllRepositoriesRecursiveStrategy(
                                                   organizationId: String
                                                 ): Future[List[GHRepository]] = {
    def getRepos(page: Int): Future[List[GHRepository]] = getRepositories(organizationId, page)

    paginate(1, MaxReposPerPage)(getRepos)
  }

}
