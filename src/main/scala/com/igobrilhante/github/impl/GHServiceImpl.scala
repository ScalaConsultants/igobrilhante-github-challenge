package com.igobrilhante.github.impl

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.http.javadsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import akka.http.scaladsl.model._
import HttpMethods._

import com.igobrilhante.GHSystem
import com.igobrilhante.github.api.GHService
import com.igobrilhante.github.models.{GHCommit, GHContributor, GHOrganization, GHRepository}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import HttpProtocols._
import MediaTypes._
import HttpCharsets._
import akka.http.scaladsl.model.headers.BasicHttpCredentials

import com.igobrilhante.github.commons.Logging

class GHServiceImpl()(implicit val system: ActorSystem[_], val ec: ExecutionContext) extends GHService with Logging {

  private val mat               = Materializer.matFromSystem
  private val BaseApi           = "https://api.github.com"
  private val http              = Http(system)
  private val MaxReposPerPage   = 100
  private val config            = system.settings.config
  private val githubConfig      = config.getConfig("github")
  private val GitHubUser        = githubConfig.getString("username")
  private val GitHubToken       = githubConfig.getString("token")
  private val GitHubCredentials = BasicHttpCredentials(GitHubUser, GitHubToken)

  override def getOrganization(organizationId: String): Future[GHOrganization] = {
    val uri = s"$BaseApi/orgs/${organizationId}"
    for {
      response <- request(uri)
      list     <- Unmarshal(response).to[GHOrganization]
    } yield list
  }

  override def getRepositories(organizationId: String, page: Int = 1): Future[List[GHRepository]] = {
    val uri = s"$BaseApi/orgs/${organizationId}/repos?page=$page&per_page=$MaxReposPerPage"
    for {
      response <- request(uri)
      list     <- Unmarshal(response).to[List[GHRepository]]
    } yield list

  }

  override def getContributors(organizationId: String, repositoryId: String): Future[List[GHContributor]] = {
    val uri = s"$BaseApi/repos/${organizationId}/${repositoryId}/contributors"
    for {
      response <- request(uri)
      list     <- Unmarshal(response).to[List[GHContributor]]
    } yield list
  }

  def getRankedContributors(organizationId: String): Future[List[GHContributor]] = {

    def rankContributors(list: List[(GHRepository, List[GHContributor])]) = {

      list
        .flatMap { case (repo, contributors) => contributors.map((_, repo)) }
        .groupBy(_._1.login)
        .map {
          case (_, contributorRepositories) =>
            val totalContributions = contributorRepositories.map(_._1.contributions).sum
            val contributor        = contributorRepositories.head._1
            contributor.copy(contributions = totalContributions)
        }
        .toList
        .sortBy(_.contributions)(Ordering[Int].reverse)

    }

    for {
      allRepositories <- getAllRepositories(organizationId)
      reposAndContributors <-
        Future.sequence(allRepositories.map(repo => getContributors(organizationId, repo.name).map(c => (repo, c))))
      result = rankContributors(reposAndContributors)
    } yield result
  }

  def getAllRepositories(organizationId: String): Future[List[GHRepository]] = {
    getAllRepositoriesStrategy(organizationId)
  }

  private def getAllRepositoriesStrategy(organizationId: String): Future[List[GHRepository]] = {

    def test(buckets: Int) = {
      (1 to buckets)
        .map(page => getRepositories(organizationId, page))
        .foldLeft(Future.successful(List.empty[GHRepository])) {
          case (result, future) =>
            for {
              currentList <- result
              next        <- future
            } yield (currentList ++ next)
        }
    }

    for {
      org <- getOrganization(organizationId)
      total   = org.total
      buckets = math.ceil(total / MaxReposPerPage.toDouble).toInt
      result <- test(buckets)
    } yield result
  }

  private def getAllRepositoriesRecursive(organizationId: String): Future[List[GHRepository]] = {
    def getRepos(page: Int): Future[List[GHRepository]] = {
      for {
        repos <- getRepositories(organizationId, page)
        result <- {
          if (repos.length < MaxReposPerPage) Future.successful(repos)
          else getRepos(page + 1).map(_ ++ repos)
        }
      } yield result
    }
    getRepos(page = 1)
  }

  private def request(uri: String) =
    http.singleRequest(
      HttpRequest(uri = uri).withHeaders(
        headers.Authorization(GitHubCredentials)
      )
    )

//  private def request2[A](uri: String)(implicit um: Unmarshaller[HttpResponse, A]) =
//    http
//      .singleRequest(HttpRequest(uri = uri))
//      .flatMap(response => Unmarshal(response).to[A](um, ec, mat))

}
