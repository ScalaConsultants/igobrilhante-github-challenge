package com.igobrilhante.github.zio.infraestructure.apis.github.future

import scala.language.postfixOps

import com.igobrilhante.github.zio.application.commons.Logging
import com.igobrilhante.github.zio.application.services.GHService
import com.igobrilhante.github.zio.domain.entities.{GHContributor, GHOrganization, GHRepository, OrganizationNotFound}
import com.igobrilhante.github.zio.interfaces.adapters.CircleJsonEntitiesAdapters._
import io.circe.Decoder
import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.circe._
import sttp.model.Uri
import zio.Task

class GHServiceTask() extends GHService[Task] with Logging {
  private val BaseApi                = "https://api.github.com"
  private val MaxReposPerPage        = 100
  private val MaxContributorsPerPage = 100
  private def httpClient             = AsyncHttpClientZioBackend()

  override def getOrganization(organizationId: String): Task[Option[GHOrganization]] = {
    val uri = uri"$BaseApi/orgs/$organizationId"

    for {
      result <- makeRequest[Option[GHOrganization]](uri).catchSome { case _ =>
        Task.fail(OrganizationNotFound(organizationId))
      }
    } yield result
  }

  override def getRepositories(organizationId: String, page: Int): Task[List[GHRepository]] = {
    val uri = uri"$BaseApi/orgs/$organizationId/repos?page=$page&per_page=${MaxReposPerPage}"

    for {
      result <- makeRequest[List[GHRepository]](uri)
    } yield result
  }

  override def getAllRepositories(organizationId: String): Task[List[GHRepository]] = {

    def get(page: Int) = getRepositories(organizationId, page)

    paginate(1, MaxReposPerPage)(get)
  }

  override def getContributors(organizationId: String, repositoryId: String, page: Int): Task[List[GHContributor]] = {
    val uri =
      uri"$BaseApi/repos/$organizationId/$repositoryId/contributors?per_page=${MaxContributorsPerPage}&page=$page"

    for {
      result <- makeRequest[List[GHContributor]](uri)
    } yield result
  }

  override def getAllContributors(organizationId: String, repositoryId: String): Task[List[GHContributor]] = {
    def get(page: Int) = getContributors(organizationId, repositoryId, page)
    paginate(1, MaxReposPerPage)(get)
  }

  private def makeRequest[A: Decoder](uri: Uri) = {
    for {
      backend <- httpClient
      result <- basicRequest
        .get(uri)
        .response(asJson[A])
        .send(backend)
        .flatMap(handleResult)
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
}
