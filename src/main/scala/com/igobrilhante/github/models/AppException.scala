package com.igobrilhante.github.models

import scala.util.control.NoStackTrace

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

sealed trait AppException extends NoStackTrace {
  def message: String
}

sealed trait NotFoundException extends AppException

object NotFoundException {
  implicit val encoder: Encoder[NotFoundException] = (e: NotFoundException) =>
    Json.obj(
      ("message", Json.fromString(e.message))
    )
}

case class OrganizationNotFound(id: String) extends NotFoundException {
  def message: String = s"Organization '$id' not found"
}

case class RepositoryNotFound(id: String) extends NotFoundException {
  def message: String = s"Repository '$id' not found"
}

case class UnexpectedErrorException private(
                                             message: String = "A unexpected error happened",
                                             details: Option[String] = None
                                           ) extends AppException

object UnexpectedErrorException {
  def create(): UnexpectedErrorException = UnexpectedErrorException()

  def withDetail(details: String): UnexpectedErrorException = UnexpectedErrorException(details = Some(details))

  implicit val decoder: Decoder[UnexpectedErrorException] = deriveDecoder
  implicit val encoder: Encoder[UnexpectedErrorException] = deriveEncoder
}

//case class GitHubApiException() extends AppException
