package com.igobrilhante.github.interfaces.gateway

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.{HttpMethods, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.CachingDirectives._
import akka.http.scaladsl.server.{RequestContext, Route}

import com.igobrilhante.github.interfaces.controllers.{Controller, Modules}

object Gateway {

  def apply(modules: Modules)(implicit system: ActorSystem[_]): Route = {

    val simpleKeyer: PartialFunction[RequestContext, Uri] = {
      val isGet: RequestContext => Boolean        = _.request.method == HttpMethods.GET
      val isAuthorized: RequestContext => Boolean = _.request.headers.exists(_.is(Authorization.lowercaseName))
      val result: PartialFunction[RequestContext, Uri] = {
        case r: RequestContext if isGet(r) && !isAuthorized(r) => r.request.uri
      }
      result
    }

    val myCache = routeCache[Uri](system.classicSystem)

    handleExceptions(AppExceptionHandler()) {
      concat(
        rejectEmptyResponse(
          Controller.impl(modules)
        )
      )
    }
  }
}
