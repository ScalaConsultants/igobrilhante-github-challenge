package com.igobrilhante.github.infraestructure.http

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.io.StdIn

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import com.igobrilhante.github.application.commons.Logging
import com.igobrilhante.github.infraestructure.apis.github.future.GHServiceImpl
import com.igobrilhante.github.interfaces.algorithms.StreamingRankAlgorithm
import com.igobrilhante.github.interfaces.controllers.Modules
import com.igobrilhante.github.interfaces.gateway.Gateway

object Server extends Logging {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[_] = ActorSystem(GHSystem(), "github-system")
    implicit val ec: ExecutionContextExecutor =
      system.dispatchers.lookup(DispatcherSelector.fromConfig("app.service-dispatcher"))

    val service          = new GHServiceImpl()(system, ec)
    val rankingAlgorithm = new StreamingRankAlgorithm(service)
    val apiModules       = Modules(service, rankingAlgorithm)

    val apiRoutes = Gateway(apiModules)
    val route     = Route.seal(apiRoutes)
    val host      = "localhost"
    val port      = 8080

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
