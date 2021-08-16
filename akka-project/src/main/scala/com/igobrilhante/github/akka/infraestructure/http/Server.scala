package com.igobrilhante.github.akka.infraestructure.http

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import com.igobrilhante.github.akka.infraestructure.apis.github.future.GHServiceImpl
import com.igobrilhante.github.akka.interfaces.algorithms.StreamingRankAlgorithm
import com.igobrilhante.github.akka.interfaces.controllers.Modules
import com.igobrilhante.github.akka.interfaces.gateway.Gateway
import com.igobrilhante.github.core.application.commons.Logging

object Server extends Logging {

  private def startHttpServer()(implicit system: ActorSystem[_]): Unit = {

    implicit val ec: ExecutionContextExecutor =
      system.dispatchers.lookup(DispatcherSelector.fromConfig("app.service-dispatcher"))

    val service          = new GHServiceImpl()(system, ec)
    val rankingAlgorithm = new StreamingRankAlgorithm(service)
    val apiModules       = Modules(service, rankingAlgorithm)

    val apiRoutes = Gateway(apiModules)
    val routes    = Route.seal(apiRoutes)
    val host      = "0.0.0.0"
    val port      = 8080

    val futureBinding = Http().newServerAt(host, port).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        logger.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      startHttpServer()(context.system)

      Behaviors.empty
    }
    val system  = ActorSystem[Nothing](rootBehavior, "github-system")

  }

}
