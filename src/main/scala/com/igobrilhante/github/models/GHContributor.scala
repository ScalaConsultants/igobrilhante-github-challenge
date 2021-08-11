package com.igobrilhante.github.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class GHContributor(id: Long, login: String, contributions: Int)

object GHContributor {

  implicit val decoder: Decoder[GHContributor] = deriveDecoder
  implicit val encoder: Encoder[GHContributor] = deriveEncoder

}
