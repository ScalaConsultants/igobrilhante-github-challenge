package com.igobrilhante.github.api

import scala.concurrent.Future

import com.igobrilhante.github.models.GHContributor

trait RankAlgorithm {

  def computeRanking(organizationId: String): Future[Option[List[GHContributor]]]

}
