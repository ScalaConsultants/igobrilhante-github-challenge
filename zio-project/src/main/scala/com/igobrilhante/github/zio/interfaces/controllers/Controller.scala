package com.igobrilhante.github.zio.interfaces.controllers

import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Async
import cats.syntax.all._
import com.igobrilhante.github.core.application.algorithms.RankAlgorithm
import com.igobrilhante.github.zio.interfaces.adapters.Http4sJsonEntitiesAdapters._
import com.igobrilhante.github.zio.interfaces.adapters.{ServiceErrorHandler, ServiceErrorhandlerAdapter}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Server
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object Controller {

  def dsl[F[_]: Async](rankAlgorithm: RankAlgorithm[F]): Http[F, F] = {
    val http4sDsl = new Http4sDsl[F] {}
    import http4sDsl._

    val serviceErrorHandler: ServiceErrorHandler[F] = ServiceErrorhandlerAdapter.impl[F]

    val service: HttpRoutes[F] = HttpRoutes
      .of[F] {
        case GET -> Root / "org" / orgId / "contributors" =>
          Ok(rankAlgorithm.computeRanking(orgId))

        case GET -> Root =>
          Ok("ok")
      }
      .handleErrorWith { error =>
        Kleisli { _ =>
          OptionT {
            serviceErrorHandler.handle(error).map(Option(_))
          }
        }
      }

    val httpApp = service.orNotFound
      .map(_.putHeaders(Server(ProductId("http4s"))))

    val routesWithLogger = Logger.httpApp[F](logHeaders = true, logBody = false)(httpApp)

    routesWithLogger
  }

}
