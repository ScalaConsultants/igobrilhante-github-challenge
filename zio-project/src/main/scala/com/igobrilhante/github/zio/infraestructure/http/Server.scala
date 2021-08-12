package com.igobrilhante.github.zio.infraestructure.http

import com.igobrilhante.github.zio.application.commons.Logging

object Server extends Logging {

  def main(args: Array[String]): Unit = {

//    implicit val system: ActorSystem[_] = ActorSystem(GHSystem(), "github-system")
//    implicit val ec: ExecutionContextExecutor =
//      system.dispatchers.lookup(DispatcherSelector.fromConfig("app.service-dispatcher"))
//
//    val service          = new GHServiceImpl()(system, ec)
//    val rankingAlgorithm = new StreamingRankAlgorithm(service)
//    val apiModules       = controllers.Modules(service, rankingAlgorithm)
//
//    val apiRoutes = gateway.Gateway(apiModules)
//    val route     = Route.seal(apiRoutes)
//    val host      = "localhost"
//    val port      = 8080
//
//    val bindingFuture = Http().newServerAt(host, port).bind(route)
//
//    logger.info(s"Server now online. Please navigate to http://$host:8080\nPress RETURN to stop...")
//    StdIn.readLine()
//    bindingFuture
//      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 30.seconds))
//      .flatMap(_.unbind())
//      .onComplete { status =>
//        logger.info(s"Terminating the system with $status")
//        system.terminate()
//      }

  }

}
