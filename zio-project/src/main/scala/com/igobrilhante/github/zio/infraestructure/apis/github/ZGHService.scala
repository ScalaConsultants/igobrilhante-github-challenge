package com.igobrilhante.github.zio.infraestructure.apis.github

import scala.language.postfixOps

import com.igobrilhante.github.adapters.json.circle.CircleJsonEntitiesAdapters._
import com.igobrilhante.github.core.application.commons.Logging
import com.igobrilhante.github.core.application.services.GHService
import com.igobrilhante.github.core.entities._
import com.typesafe.config.ConfigFactory
import io.circe.Decoder
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.circe._
import sttp.model.{StatusCode, Uri}
import zio.Task

class ZGHService(httpClient: SttpBackend[Task, ZioStreams with WebSockets]) extends GHService[Task] with Logging {
  private val BaseApi                = "https://api.github.com"
  private val MaxReposPerPage        = 100
  private val MaxContributorsPerPage = 100
  private val config                 = ConfigFactory.load()
  private val githubConfig           = config.getConfig("github")
  private val GitHubToken            = "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"

  override def getOrganization(organizationId: String): Task[Option[GHOrganization]] = {
    val uri = uri"$BaseApi/orgs/$organizationId"

    for {
      result <- makeRequest[Option[GHOrganization]](uri)
        .catchSome {
          catchNoContent(OrganizationNotFound(organizationId))
        }
        .flatMap(handleResult)
    } yield result
  }

  override def getRepositories(organizationId: String, page: Int): Task[List[GHRepository]] = {
    val uri = uri"$BaseApi/orgs/$organizationId/repos?page=$page&per_page=$MaxReposPerPage"

    for {
      result <- makeRequest[List[GHRepository]](uri)
        .map(catchNoContentList)
        .flatMap(handleResult)
    } yield result
  }

  override def getAllRepositories(organizationId: String): Task[List[GHRepository]] = {

    def get(page: Int) = getRepositories(organizationId, page)

    paginate(1, MaxReposPerPage)(get)
  }

  override def getContributors(organizationId: String, repositoryId: String, page: Int): Task[List[GHContributor]] = {
    val uri =
      uri"$BaseApi/repos/$organizationId/$repositoryId/contributors?per_page=$MaxContributorsPerPage&page=$page"

    for {
      result <- makeRequest[List[GHContributor]](uri)
        .map(catchNoContentList)
        .flatMap(handleResult)
    } yield result
  }

  override def getAllContributors(organizationId: String, repositoryId: String): Task[List[GHContributor]] = {
    def get(page: Int) = getContributors(organizationId, repositoryId, page)
    paginate(1, MaxReposPerPage)(get)
  }

  private def makeRequest[A: Decoder](uri: Uri) = {
    for {
      result <- basicRequest
        .header("Authorization", s"Bearer $GitHubToken")
        .get(uri)
        .response(asJson[A])
        .send(httpClient)
//        .flatMap(handleResult)
    } yield result
  }

  private def handleResult[A, B, C](response: Response[Either[ResponseException[A, B], C]]) = {
    response.body match {
      case Left(e)     => Task.fail(e)
      case Right(body) => Task.succeed(body)
    }
  }

  private def paginate[A](page: Int, maxPerPage: Int)(fn: Int => Task[List[A]]): Task[List[A]] = {
    for {
      repo <- fn(page)
      result <- {
        if (repo.length < maxPerPage) Task.succeed(repo)
        else paginate(page + 1, maxPerPage)(fn).map(_ ++ repo)
      }
    } yield result
  }

  def catchNoContentList[A, B, C <: List[_]](
      response: Response[Either[ResponseException[A, B], C]]
  ): Response[Either[ResponseException[A, B], C]] = {
    response.code match {
      case StatusCode.NoContent => Response.ok(Right(List.empty.asInstanceOf[C])) // TODO how to avoid the asInstanceOf
      case _                    => response
    }
  }

  private def catchNoContent[A](fn: => AppException): PartialFunction[Throwable, Task[A]] = {
    case HttpError(_, StatusCode.NotFound) =>
      Task.fail(fn)
  }

}
