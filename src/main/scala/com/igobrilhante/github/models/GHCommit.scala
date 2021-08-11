package com.igobrilhante.github.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class GHCommit()

object GHCommit {
  implicit val decoder: Decoder[GHCommit] = deriveDecoder
  implicit val encoder: Encoder[GHCommit] = deriveEncoder
}
