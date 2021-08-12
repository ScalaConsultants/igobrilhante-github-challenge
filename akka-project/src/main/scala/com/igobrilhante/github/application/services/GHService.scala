package com.igobrilhante.github.application.services

import scala.concurrent.Future

import com.igobrilhante.github.domain.entities._

trait GHService {

  def getOrganization(organizationId: String): Future[Option[GHOrganization]]

  def getRepositories(organizationId: String, page: Int): Future[List[GHRepository]]

  def getAllRepositories(organizationId: String): Future[List[GHRepository]]

  def getContributors(organizationId: String, repositoryId: String, page: Int): Future[List[GHContributor]]

  def getAllContributors(organizationId: String, repositoryId: String): Future[List[GHContributor]]

}
