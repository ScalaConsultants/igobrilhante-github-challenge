package com.igobrilhante.github.zio.application.usecases

import com.igobrilhante.github.zio.application.algorithms.RankAlgorithm
import com.igobrilhante.github.zio.domain.entities.GHContributor

case class GetRankedContributorsUseCase[F[_]](algorithm: RankAlgorithm[F])
    extends UseCase[F, String, Option[List[GHContributor]]] {

  def execute(organizationId: String): F[Option[List[GHContributor]]] = algorithm.computeRanking(organizationId)

}
