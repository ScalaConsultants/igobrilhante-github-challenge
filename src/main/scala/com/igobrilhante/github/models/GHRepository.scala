package com.igobrilhante.github.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class GHRepository(id: Long, name: String, `private`: Boolean)

object GHRepository {

  implicit val decoder: Decoder[GHRepository] = deriveDecoder
  implicit val encoder: Encoder[GHRepository] = deriveEncoder

}
