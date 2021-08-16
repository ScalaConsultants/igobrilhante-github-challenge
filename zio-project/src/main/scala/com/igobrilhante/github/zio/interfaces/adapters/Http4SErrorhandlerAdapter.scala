package com.igobrilhante.github.zio.interfaces.adapters

import cats.effect.Async
import com.igobrilhante.github.core.entities.{NotFoundException, UnexpectedErrorException}
import com.igobrilhante.github.zio.interfaces.adapters.Http4sJsonEntitiesAdapters._
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}

/** HTTP Server error handler adapter for http4s. */
object Http4SErrorhandlerAdapter {

  def create[F[_]: Async](request: Request[F]): PartialFunction[Throwable, F[Response[F]]] = {
    val http4sDsl = new Http4sDsl[F] {}
    import http4sDsl._
    val partialFunction: PartialFunction[Throwable, F[Response[F]]] = {
      case ex: UnexpectedErrorException =>
        InternalServerError(ex)
      case ex: Throwable =>
        InternalServerError(UnexpectedErrorException.fromThrowable(ex))
    }

    partialFunction
  }

}

trait ServiceErrorHandler[F[_]] {
  def handle(error: Throwable): F[Response[F]]
}

/** HTTP Service error handler Handling error at Service level helps us during the tests, since we can deal with http
  * response instead of exception.
  */
object ServiceErrorhandlerAdapter {

  def impl[F[_]: Async]: ServiceErrorHandler[F] = new ServiceErrorHandler[F] with Http4sDsl[F] {
    override def handle(error: Throwable): F[Response[F]] = {
      error match {
        case notFound: NotFoundException =>
          NotFound(notFound)
        case ex: UnexpectedErrorException =>
          InternalServerError(ex)
        case ex: Throwable =>
          InternalServerError(UnexpectedErrorException.fromThrowable(ex))
      }
    }
  }

}
