package com.igobrilhante.github.akka.interfaces.gateway

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import com.igobrilhante.github.akka.interfaces.controllers.{Controller, Modules}

object Gateway {

  def apply(modules: Modules)(implicit system: ActorSystem[_]): Route = {

    handleExceptions(AppExceptionHandler()) {
      concat(
        rejectEmptyResponse(
          Controller.impl(modules)
        )
      )
    }
  }
}
