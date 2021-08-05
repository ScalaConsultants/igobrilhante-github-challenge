package com.igobrilhante.github.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class GHContributor(id: Long, login: String, contributions: Int)

object GHContributor {

  implicit val decoder: Decoder[GHContributor] = deriveDecoder
  implicit val encoder: Encoder[GHContributor] = deriveEncoder

}
