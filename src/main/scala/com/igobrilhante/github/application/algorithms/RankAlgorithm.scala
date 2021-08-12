package com.igobrilhante.github.application.algorithms

import com.igobrilhante.github.domain.entities.GHContributor

trait RankAlgorithm[F[_]] {

  def computeRanking(organizationId: String): F[Option[List[GHContributor]]]

}
