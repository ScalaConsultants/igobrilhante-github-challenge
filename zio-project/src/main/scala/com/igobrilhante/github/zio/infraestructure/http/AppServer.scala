package com.igobrilhante.github.zio.infraestructure.http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import cats.effect.{Async, Resource}
import com.igobrilhante.github.core.application.algorithms.RankAlgorithm
import com.igobrilhante.github.core.application.services.GHService
import com.igobrilhante.github.zio.interfaces.adapters.Http4SErrorhandlerAdapter
import com.igobrilhante.github.zio.interfaces.controllers.Controller
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

trait AppServer {

  def server[F[_]: Async](routes: HttpApp[F]): Resource[F, Server]

  def server[F[_]: Async](service: GHService[F], algorithm: RankAlgorithm[F]): Resource[F, Server]

}

object AppServer {

  def dsl(): AppServer = new AppServer {

    def server[F[_]: Async](routes: HttpApp[F]): Resource[F, Server] = {

      BlazeServerBuilder[F](global)
        .withResponseHeaderTimeout(180.seconds)
        .withIdleTimeout(190.seconds)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(routes)
        .withServiceErrorHandler(Http4SErrorhandlerAdapter.create)
        .resource
    }

    def server[F[_]: Async](service: GHService[F], algorithm: RankAlgorithm[F]): Resource[F, Server] = {
      val routes = Controller.dsl[F](algorithm)
      for {
        instance <- server(routes)
      } yield (instance)
    }

  }

}
