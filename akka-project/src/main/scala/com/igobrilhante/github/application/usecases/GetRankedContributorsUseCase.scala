package com.igobrilhante.github.application.usecases

import com.igobrilhante.github.application.algorithms.RankAlgorithm
import com.igobrilhante.github.domain.entities.GHContributor

case class GetRankedContributorsUseCase[F[_]](algorithm: RankAlgorithm[F])
    extends UseCase[F, String, Option[List[GHContributor]]] {

  def execute(organizationId: String): F[Option[List[GHContributor]]] = algorithm.computeRanking(organizationId)

}
