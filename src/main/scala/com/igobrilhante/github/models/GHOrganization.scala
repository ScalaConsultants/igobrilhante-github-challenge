package com.igobrilhante.github.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class GHOrganization(
                           id: Long,
                           login: String,
                           public_repos: Int,
                           total_private_repos: Option[Int],
                           name: Option[String]
                         ) {

  def total: Int = public_repos + total_private_repos.getOrElse(0)

}

object GHOrganization {
  implicit val decoder: Decoder[GHOrganization] = deriveDecoder
  implicit val encoder: Encoder[GHOrganization] = deriveEncoder
}
