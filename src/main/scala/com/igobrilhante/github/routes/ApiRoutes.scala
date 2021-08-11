package com.igobrilhante.github.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.{HttpMethods, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.CachingDirectives._
import akka.http.scaladsl.server.{RequestContext, Route}

import com.igobrilhante.github.api.{GHService, RankAlgorithm}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

object ApiRoutes {

  case class Modules(ghService: GHService, rankAlgorithm: RankAlgorithm)

  def apply(modules: Modules)(implicit system: ActorSystem[_]): Route = {

    val Modules(ghService, rankAlgorithm) = modules

    val simpleKeyer: PartialFunction[RequestContext, Uri] = {
      val isGet: RequestContext => Boolean = _.request.method == HttpMethods.GET
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
          pathPrefix("org") {
            (path(Segment / "contributors") & get) { orgId =>
              //            cache(myCache, simpleKeyer) {
              complete(rankAlgorithm.computeRanking(orgId))
              //            }
            }
          }
        )
      )
    }
  }
}
