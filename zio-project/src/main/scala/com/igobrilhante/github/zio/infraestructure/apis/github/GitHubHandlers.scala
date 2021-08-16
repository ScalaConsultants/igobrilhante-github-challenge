package com.igobrilhante.github.zio.infraestructure.apis.github

import com.igobrilhante.github.core.entities.{AppException, UnexpectedErrorException}
import com.typesafe.config.ConfigFactory
import io.circe
import io.circe.Decoder
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.circe.asJson
import sttp.client3.{Response, ResponseException, SttpBackend, basicRequest}
import sttp.model.{StatusCode, Uri}
import zio.Cause.Fail
import zio.logging.{Logging, log}
import zio.{Task, ZIO}

private[github] trait GitHubHandlers {

  private val config       = ConfigFactory.load()
  private val githubConfig = config.getConfig("github")
  private val GitHubToken  = githubConfig.getString("token")

  val httpClient: SttpBackend[Task, ZioStreams with WebSockets]

  protected def makeRequest[A: Decoder](
      uri: Uri
  ): ZIO[Any, Throwable, Response[Either[ResponseException[String, circe.Error], A]]] = {
    for {
      result <- basicRequest
        .header("Authorization", s"Bearer $GitHubToken")
        .get(uri)
        .response(asJson[A])
        .send(httpClient)
    } yield result
  }

  protected def handleResult[A, B, C](
      fn: => AppException = UnexpectedErrorException.create()
  )(response: Response[Either[ResponseException[A, B], C]]): ZIO[Logging, Throwable, C] = response.body match {
    case Left(e) =>
      for {
        _ <- {
          if (response.isServerError) log.error(e.getMessage, Fail(e))
          else if (response.isClientError) log.error(e.getMessage, Fail(e))
          else Task.none
        }
        fail <- Task.fail(fn)
      } yield fail
    case Right(body) => Task.succeed(body)
  }

  protected def paginate[A](page: Int, maxPerPage: Int)(fn: Int => Task[List[A]]): Task[List[A]] = {
    for {
      repo <- fn(page)
      result <- {
        if (repo.length < maxPerPage) Task.succeed(repo)
        else paginate(page + 1, maxPerPage)(fn).map(_ ++ repo)
      }
    } yield result
  }

  /* GitHub API returns 204 (NoContent) for empty lists.
     Then, it handles 204 responses by returning an empty list */
  protected def handleNoContentList[A, B, C <: List[_]](
      response: Response[Either[ResponseException[A, B], C]]
  ): Response[Either[ResponseException[A, B], C]] = {
    response.code match {
      case StatusCode.NoContent => Response.ok(Right(List.empty.asInstanceOf[C])) // TODO how to avoid the asInstanceOf
      case _                    => response
    }
  }

}
