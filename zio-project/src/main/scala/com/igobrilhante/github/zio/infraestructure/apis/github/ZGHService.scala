package com.igobrilhante.github.zio.infraestructure.apis.github

import scala.language.postfixOps

import com.igobrilhante.github.adapters.json.circle.CircleJsonEntitiesAdapters._
import com.igobrilhante.github.core.application.services.GHService
import com.igobrilhante.github.core.entities._
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import zio.Task
import zio.logging._
import zio.logging.slf4j.Slf4jLogger

/** GHService implementation based on ZIO Task.
  */
class ZGHService(val httpClient: SttpBackend[Task, ZioStreams with WebSockets])
    extends GHService[Task]
    with GitHubHandlers {
  private val BaseApi                = "https://api.github.com"
  private val MaxReposPerPage        = 100
  private val MaxContributorsPerPage = 100

  private val logFormat = "%s"
  private val loggerLayer =
    Slf4jLogger.make { (_, message) => logFormat.format(message) }

  override def getOrganization(organizationId: String): Task[Option[GHOrganization]] = {
    val uri = uri"$BaseApi/orgs/$organizationId"

    val result = for {
      _        <- log.info(s"getOrganization organization $organizationId")
      response <- makeRequest[Option[GHOrganization]](uri)
      result   <- handleResult(OrganizationNotFound(organizationId))(response)
    } yield result

    result.provideLayer(loggerLayer)
  }

  override def getRepositories(organizationId: String, page: Int): Task[List[GHRepository]] = {
    val uri = uri"$BaseApi/orgs/$organizationId/repos?page=$page&per_page=$MaxReposPerPage"
    val result = for {
      _        <- log.info(s"getRepositories organization $organizationId page $page")
      response <- makeRequest[List[GHRepository]](uri).map(handleNoContentList)
      result   <- handleResult()(response)
    } yield result

    result.provideLayer(loggerLayer)
  }

  override def getAllRepositories(organizationId: String): Task[List[GHRepository]] = {

    def get(page: Int) = getRepositories(organizationId, page)

    paginate(1, MaxReposPerPage)(get)
  }

  override def getContributors(organizationId: String, repositoryId: String, page: Int): Task[List[GHContributor]] = {
    val uri =
      uri"$BaseApi/repos/$organizationId/$repositoryId/contributors?per_page=$MaxContributorsPerPage&page=$page"

    val result = for {
      _        <- log.info(s"getContributors organization $organizationId repository $repositoryId page $page")
      response <- makeRequest[List[GHContributor]](uri).map(handleNoContentList)
      result   <- handleResult()(response)
    } yield result

    result.provideLayer(loggerLayer)
  }

  override def getAllContributors(organizationId: String, repositoryId: String): Task[List[GHContributor]] = {
    def get(page: Int) = getContributors(organizationId, repositoryId, page)
    paginate(1, MaxReposPerPage)(get)
  }

}
