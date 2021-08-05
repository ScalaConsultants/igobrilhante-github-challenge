package com.igobrilhante.github.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class GHCommit()

object GHCommit {
  implicit val decoder: Decoder[GHCommit] = deriveDecoder
  implicit val encoder: Encoder[GHCommit] = deriveEncoder
}
