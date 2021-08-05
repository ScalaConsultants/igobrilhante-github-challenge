package com.igobrilhante.github.tests

import com.igobrilhante.github.api.GHService
import com.igobrilhante.github.impl.GHServiceImpl

class GHServicePublicSpec extends GHSpec {

  val service: GHService = new GHServiceImpl()

  val organizationId = "ScalaConsultants"
  val publicRepo     = "lift-rest-demo"

  "GHService Public Spec" should "get public repositories for ScalaConsultants" in {

    val list = service.getRepositories("ScalaConsultants", page = 1).futureValue(timeout)

    list should not be empty

  }

  it should s"get the organization '$organizationId'" in {
    val org = service.getOrganization(organizationId).futureValue(timeout)

    org.login shouldEqual organizationId

  }

  it should "get a list of contributors for public repo 'lift-rest-demo'" in {
    val list = service.getContributors(organizationId, publicRepo).futureValue(timeout)

    list should not be empty
  }

  it should s"get all repositories for $organizationId" in {
    val org  = service.getOrganization(organizationId).futureValue(timeout)
    val list = service.getAllRepositories(organizationId).futureValue(timeout)

    list should have size org.total
    list.filter(_.`private`) should have size org.total_private_repos.getOrElse(0).toLong
    list.filterNot(_.`private`) should have size org.public_repos
  }

}
