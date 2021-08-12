package com.igobrilhante.github.zio.interfaces.adapters

import com.igobrilhante.github.zio.domain.entities.{GHCommit, GHContributor, GHOrganization, GHRepository}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

object CircleJsonEntitiesAdapters extends FailFastCirceSupport {

  implicit lazy val commitDecoder: Decoder[GHCommit] = deriveDecoder
  implicit lazy val commitEncoder: Encoder[GHCommit] = deriveEncoder

  implicit lazy val contributorsDecoder: Decoder[GHContributor] = deriveDecoder
  implicit lazy val contributorsEncoder: Encoder[GHContributor] = deriveEncoder

  implicit lazy val orgDecoder: Decoder[GHOrganization] = deriveDecoder
  implicit lazy val orgEncoder: Encoder[GHOrganization] = deriveEncoder

  implicit lazy val repoDecoder: Decoder[GHRepository] = deriveDecoder
  implicit lazy val repoEncoder: Encoder[GHRepository] = deriveEncoder

}
