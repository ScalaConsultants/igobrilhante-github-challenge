package com.igobrilhante.github.core.application.services

import com.igobrilhante.github.core.entities._

trait GHService[F[_]] {

  def getOrganization(organizationId: String): F[Option[GHOrganization]]

  def getRepositories(organizationId: String, page: Int): F[List[GHRepository]]

  def getAllRepositories(organizationId: String): F[List[GHRepository]]

  def getContributors(organizationId: String, repositoryId: String, page: Int): F[List[GHContributor]]

  def getAllContributors(organizationId: String, repositoryId: String): F[List[GHContributor]]

}
