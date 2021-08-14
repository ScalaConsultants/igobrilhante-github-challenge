package com.igobrilhante.github.core.application.algorithms

import com.igobrilhante.github.core.entities.GHContributor

trait RankAlgorithm[F[_]] {

  def computeRanking(organizationId: String): F[Option[List[GHContributor]]]

}
