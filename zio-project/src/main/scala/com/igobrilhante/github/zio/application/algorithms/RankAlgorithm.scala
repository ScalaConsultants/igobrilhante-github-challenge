package com.igobrilhante.github.zio.application.algorithms

import com.igobrilhante.github.zio.domain.entities.GHContributor

trait RankAlgorithm[F[_]] {

  def computeRanking(organizationId: String): F[Option[List[GHContributor]]]

}
