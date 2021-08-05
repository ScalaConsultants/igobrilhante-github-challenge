package com.igobrilhante.github

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import com.igobrilhante.GHSystem
import com.igobrilhante.github.api.GHService
import com.igobrilhante.github.impl.GHServiceImpl
import com.igobrilhante.github.modules.GHServiceModule
import com.igobrilhante.github.routes.ApiRoutes

object Server extends GHServiceModule {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[_] = ActorSystem(GHSystem(), "github-system")
    implicit val ec: ExecutionContextExecutor =
      system.dispatchers.lookup(DispatcherSelector.fromConfig("app.service-dispatcher"))

    val service: GHService = new GHServiceImpl()(system, ec)

    val apiRoutes = ApiRoutes(service)
    val route     = Route.seal(apiRoutes)

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server now online. Please navigate to http://localhost:8080\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
