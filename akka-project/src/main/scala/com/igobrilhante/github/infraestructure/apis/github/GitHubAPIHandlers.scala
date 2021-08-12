package com.igobrilhante.github.infraestructure.apis.github

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.OAuth2BearerToken

import com.igobrilhante.github.application.commons.{CacheHeader, Logging}
import com.igobrilhante.github.domain.entities.{AppException, UnexpectedErrorException}
import com.typesafe.config.ConfigFactory

private[github] trait GitHubAPIHandlers {
  this: Logging =>

  private val config            = ConfigFactory.load()
  private val githubConfig      = config.getConfig("github")
  private val GitHubToken       = githubConfig.getString("token")
  private val GitHubCredentials = OAuth2BearerToken(GitHubToken)

  def http: HttpExt

  def paginate[A](page: Int, maxPerPage: Int)(
      fn: Int => Future[List[A]]
  )(implicit ec: ExecutionContext): Future[List[A]] = {
    for {
      repo <- fn(page)
      result <- {
        if (repo.length < maxPerPage) Future.successful(repo)
        else paginate(page + 1, maxPerPage)(fn).map(_ ++ repo)
      }
    } yield result
  }

  def request(uri: String)(implicit ec: ExecutionContext): Future[HttpResponse] = {

    def internalCache(uri: String): Future[Option[CacheHeader]] = {
      Future.successful(None)
    }

    // TODO consider headers ETAG and Last-Modified to void consuming limits of Github API
    def handleCacheHeaders(optCache: Option[CacheHeader]): Seq[HttpHeader] = {
      optCache match {
        case Some(cacheHeader) =>
          cacheHeader.etag.fold(Seq.empty[HttpHeader]) { etag => Seq(headers.ETag(etag)) }
          cacheHeader.lastModified.fold(Seq.empty[HttpHeader]) { last =>
            Seq(headers.`If-Modified-Since`(DateTime(last)))
          }
        case None =>
          // simplesmente envia a requisição
          Seq()
      }
    }

    def makeRequest(httpRequest: HttpRequest) =
      http
        .singleRequest(httpRequest)
        .map(interceptResponseFor(uri))

    def addCredentials(extraHeaders: Seq[HttpHeader]) = {
      HttpRequest(uri = uri).withHeaders(
        headers.Authorization(GitHubCredentials),
        extraHeaders: _*
      )
    }

    def handleResponse(response: HttpResponse) = {
      response.status match {
        case StatusCodes.Unauthorized =>
          Future.failed(
            UnexpectedErrorException.withDetail(s"Received 401 Unauthorized from GitHub API for uri $uri")
          )
        case _ => Future.successful(response)
      }
    }

    def handleRecover[U]: PartialFunction[Throwable, U] = {
      case e: AppException => throw e
      case error: Throwable =>
        Option(error.getMessage).fold(throw UnexpectedErrorException.create()) { details =>
          throw UnexpectedErrorException.withDetail(details)
        }
    }

    internalCache(uri)
      .map(handleCacheHeaders)
      .map(addCredentials)
      .flatMap(makeRequest)
      .flatMap(handleResponse)
      .recover(handleRecover)

  }

  def interceptResponseFor(uri: String)(response: HttpResponse): HttpResponse = {

    response.status match {
      case StatusCodes.Success(status) =>
      // Persist the ETag and Last-Modified headers
      case _ =>
        logFailedStatus(uri, response)
    }

    response
  }

  def logFailedStatus(uri: String, response: HttpResponse): Unit = {
    logger.error("Failed to request {}", uri)
    logger.error(response.toString())
    logger.error(response.headers.mkString("\n"))
  }

}
