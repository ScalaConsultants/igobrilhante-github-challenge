package com.igobrilhante.github.adapters.json.circle

import com.igobrilhante.github.core.entities._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

trait CircleJsonEntitiesAdapters {

  implicit lazy val commitDecoder: Decoder[GHCommit] = deriveDecoder
  implicit lazy val commitEncoder: Encoder[GHCommit] = deriveEncoder

  implicit lazy val contributorsDecoder: Decoder[GHContributor] = deriveDecoder
  implicit lazy val contributorsEncoder: Encoder[GHContributor] = deriveEncoder

  implicit lazy val orgDecoder: Decoder[GHOrganization] = deriveDecoder
  implicit lazy val orgEncoder: Encoder[GHOrganization] = deriveEncoder

  implicit lazy val repoDecoder: Decoder[GHRepository] = deriveDecoder
  implicit lazy val repoEncoder: Encoder[GHRepository] = deriveEncoder

  implicit val notFoundExceptionEncoder: Encoder[NotFoundException] = (e: NotFoundException) =>
    Json.obj(
      ("message", Json.fromString(e.message))
    )

  implicit val unexpectedErrorDecoder: Decoder[UnexpectedErrorException] = deriveDecoder
  implicit val unexpectedErrorEncoder: Encoder[UnexpectedErrorException] = deriveEncoder

}

object CircleJsonEntitiesAdapters extends CircleJsonEntitiesAdapters
