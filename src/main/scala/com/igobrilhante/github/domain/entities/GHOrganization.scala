package com.igobrilhante.github.domain.entities

case class GHOrganization(
    id: Long,
    login: String,
    public_repos: Int,
    total_private_repos: Option[Int],
    name: Option[String]
) {

  def total: Int = public_repos + total_private_repos.getOrElse(0)

}

object GHOrganization {}
