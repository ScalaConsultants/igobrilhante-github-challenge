package com.igobrilhante.github.core.application.algorithms

import com.igobrilhante.github.core.entities.GHContributor

/** Ranking algorithm definition. */
trait RankAlgorithm[F[_]] {

  /** Given the organization id, computes the list of contributors ordered by their number of contributions in the
    * organization.
    */
  def computeRanking(organizationId: String): F[Option[List[GHContributor]]]

}
