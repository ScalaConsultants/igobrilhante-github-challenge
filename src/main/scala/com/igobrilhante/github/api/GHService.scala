package com.igobrilhante.github.api

import scala.concurrent.Future

import com.igobrilhante.github.models.{GHContributor, GHOrganization, GHRepository}

trait GHService {

  def getOrganization(organizationId: String): Future[GHOrganization]

  def getRepositories(organizationId: String, page: Int): Future[List[GHRepository]]

  def getAllRepositories(organizationId: String): Future[List[GHRepository]]

  def getContributors(organizationId: String, repositoryId: String): Future[List[GHContributor]]

  def getRankedContributors(organizationId: String): Future[List[GHContributor]]

}
