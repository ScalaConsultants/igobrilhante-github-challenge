package com.igobrilhante.github.zio.application.services

import com.igobrilhante.github.zio.domain.entities.{GHContributor, GHOrganization, GHRepository}

trait GHService[F[_]] {

  def getOrganization(organizationId: String): F[Option[GHOrganization]]

  def getRepositories(organizationId: String, page: Int): F[List[GHRepository]]

  def getAllRepositories(organizationId: String): F[List[GHRepository]]

  def getContributors(organizationId: String, repositoryId: String, page: Int): F[List[GHContributor]]

  def getAllContributors(organizationId: String, repositoryId: String): F[List[GHContributor]]

}
