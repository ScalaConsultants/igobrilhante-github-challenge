package com.igobrilhante.github.interfaces.controllers

import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.{HttpMethods, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.CachingDirectives._
import akka.http.scaladsl.server.{RequestContext, Route}

import com.igobrilhante.github.application.algorithms.RankAlgorithm
import com.igobrilhante.github.application.services.GHService
import com.igobrilhante.github.interfaces.adapters.CircleJsonEntitiesAdapters._

case class Modules(ghService: GHService, rankAlgorithm: RankAlgorithm[Future])

object Controller {

  val simpleKeyer: PartialFunction[RequestContext, Uri] = {
    val isGet: RequestContext => Boolean = _.request.method == HttpMethods.GET
    val isAuthorized: RequestContext => Boolean = _.request.headers.exists(_.is(Authorization.lowercaseName))
    val result: PartialFunction[RequestContext, Uri] = {
      case r: RequestContext if isGet(r) && !isAuthorized(r) => r.request.uri
    }
    result
  }

  def impl(modules: Modules)(implicit system: ActorSystem[_]): Route = {
    val Modules(_, rankAlgorithm) = modules
    val myCache = routeCache[Uri](system.classicSystem)
    pathPrefix("org") {
      (path(Segment / "contributors") & get) { orgId =>
        cache(myCache, simpleKeyer) {
          complete(rankAlgorithm.computeRanking(orgId))
        }
      }
    }
  }

}
