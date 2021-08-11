package com.igobrilhante.github

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.io.StdIn

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import com.igobrilhante.GHSystem
import com.igobrilhante.github.api.{GHService, RankAlgorithm}
import com.igobrilhante.github.commons.Logging
import com.igobrilhante.github.impl.{GHServiceImpl, StreamingRankAlgorithm}
import com.igobrilhante.github.routes.ApiRoutes

object Server extends Logging {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[_] = ActorSystem(GHSystem(), "github-system")
    implicit val ec: ExecutionContextExecutor =
      system.dispatchers.lookup(DispatcherSelector.fromConfig("app.service-dispatcher"))

    val service: GHService = new GHServiceImpl()(system, ec)
    val rankingAlgorithm: RankAlgorithm = new StreamingRankAlgorithm(service)
    val apiModules = ApiRoutes.Modules(service, rankingAlgorithm)

    val apiRoutes = ApiRoutes(apiModules)
    val route = Route.seal(apiRoutes)
    val host = "localhost"
    val port = 8080

    val bindingFuture = Http().newServerAt(host, port).bind(route)

    logger.info(s"Server now online. Please navigate to http://$host:8080\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 30.seconds))
      .flatMap(_.unbind())
      .onComplete { status =>
        logger.info(s"Terminating the system with $status")
        system.terminate()
      }

  }

}
