package com.igobrilhante.github.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.{HttpMethods, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route}
import akka.http.scaladsl.server.directives.CachingDirectives._

import com.igobrilhante.github.api.GHService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

object ApiRoutes {

  def apply(service: GHService)(implicit system: ActorSystem[_]): Route = {

    //Example keyer for non-authenticated GET requests
    val simpleKeyer: PartialFunction[RequestContext, Uri] = {
      val isGet: RequestContext => Boolean        = _.request.method == HttpMethods.GET
      val isAuthorized: RequestContext => Boolean = _.request.headers.exists(_.is(Authorization.lowercaseName))
      val result: PartialFunction[RequestContext, Uri] = {
        case r: RequestContext if isGet(r) && !isAuthorized(r) => r.request.uri
      }
      result
    }

    val myCache = routeCache[Uri](system.classicSystem)

    concat(
      rejectEmptyResponse(
        pathPrefix("org") {
          (path(Segment / "contributors") & get) { orgId =>
            cache(myCache, simpleKeyer) {
              complete(service.getRankedContributors(orgId))
            }
          }
        }
      )
    )
  }
}
