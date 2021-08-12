package com.igobrilhante.github.zio.interfaces.algorithms

import com.igobrilhante.github.zio.application.algorithms.RankAlgorithm
import com.igobrilhante.github.zio.domain.entities.GHContributor
import zio.Task

class ZAlgorithm() extends RankAlgorithm[Task]{
  override def computeRanking(organizationId: String): Task[Option[List[GHContributor]]] = ???
}
