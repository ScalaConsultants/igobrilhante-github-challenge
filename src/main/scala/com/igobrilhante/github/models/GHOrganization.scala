package com.igobrilhante.github.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class GHOrganization(id: Long, login: String, name: String, public_repos: Int, total_private_repos: Option[Int]) {

  def total: Int = public_repos + total_private_repos.getOrElse(0)

}

object GHOrganization {
  implicit val decoder: Decoder[GHOrganization] = deriveDecoder
  implicit val encoder: Encoder[GHOrganization] = deriveEncoder
}
