package com.igobrilhante.github.core.entities

import scala.util.control.NoStackTrace

sealed trait AppException extends NoStackTrace {
  def message: String
}

sealed trait NotFoundException extends AppException

case class OrganizationNotFound(id: String) extends NotFoundException {
  def message: String = s"Organization '$id' not found"
}

case class RepositoryNotFound(id: String) extends NotFoundException {
  def message: String = s"Repository '$id' not found"
}

case class UnexpectedErrorException private (
    message: String = "A unexpected error happened",
    details: Option[String] = None
) extends AppException

object UnexpectedErrorException {
  def create(): UnexpectedErrorException = UnexpectedErrorException()

  def withDetail(details: String): UnexpectedErrorException = UnexpectedErrorException(details = Some(details))

  def fromThrowable(error: Throwable): UnexpectedErrorException = UnexpectedErrorException(details = Some(error.getMessage))

}

//case class GitHubApiException() extends AppException
