package com.igobrilhante.github.interfaces.controllers

import scala.concurrent.Future

import akka.http.scaladsl.server.Directives._

import com.igobrilhante.github.application.algorithms.RankAlgorithm
import com.igobrilhante.github.application.services.GHService
import com.igobrilhante.github.application.usecases.GetRankedContributorsUseCase
import com.igobrilhante.github.interfaces.adapters.CircleJsonEntitiesAdapters._

case class Modules(ghService: GHService, rankAlgorithm: RankAlgorithm[Future])

object Controller {

  def impl(modules: Modules) = {
    val Modules(_, rankAlgorithm) = modules

    val getRankedContributorsUseCase = GetRankedContributorsUseCase(rankAlgorithm)

    pathPrefix("org") {
      (path(Segment / "contributors") & get) { orgId =>
        //            cache(myCache, simpleKeyer) {
        complete(getRankedContributorsUseCase.execute(orgId))
        //            }
      }
    }
  }

}
