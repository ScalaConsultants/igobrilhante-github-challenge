package com.igobrilhante.github.zio.interfaces.controllers

import cats.effect.kernel.Async
import com.igobrilhante.github.core.application.algorithms.RankAlgorithm
import com.igobrilhante.github.zio.interfaces.adapters.Http4sJsonEntitiesAdapters._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.{Http, HttpRoutes, Response}

import cats.syntax.all._

object Controller {


  def dsl[F[_]: Async](rankAlgorithm: RankAlgorithm[F]): Http[F, F] = {
    val http4sDsl = new Http4sDsl[F] {}
    import http4sDsl._
    val service = HttpRoutes
      .of[F] {
        case GET -> Root / "org" / orgId / "contributors" =>
          val res = rankAlgorithm.computeRanking(orgId)
          Ok(res)

        case GET -> Root =>
          Ok("ok")
      }
      .orNotFound

    val routesWithLogger = Logger.httpApp[F](logHeaders = true, logBody = false)(service)

    routesWithLogger
  }

}
