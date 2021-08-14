package com.igobrilhante.github.akka.interfaces.algorithms

import scala.concurrent.{ExecutionContext, Future}

import com.igobrilhante.github.core.application.algorithms.RankAlgorithm
import com.igobrilhante.github.core.application.commons.Logging
import com.igobrilhante.github.core.application.services.GHService
import com.igobrilhante.github.core.entities.{GHContributor, GHRepository}

class NaiveRankAlgorithm(service: GHService[Future])(implicit ec: ExecutionContext) extends RankAlgorithm[Future] with Logging {

  override def computeRanking(organizationId: String): Future[Option[List[GHContributor]]] = {
    logger.debug("getRankedContributors for org {}", organizationId)

    def rankContributors(list: List[(GHRepository, List[GHContributor])]) = {
      logger.debug("rankContributors for org {}", organizationId)
      val res = list
        .flatMap { case (repo, contributors) => contributors.map((_, repo)) }
        .groupBy(_._1.login)
        .map { case (_, contributorRepositories) =>
          val totalContributions = contributorRepositories.map(_._1.contributions).sum
          val contributor        = contributorRepositories.head._1
          contributor.copy(contributions = totalContributions)
        }
        .toList
        .sortBy(_.contributions)(Ordering[Int].reverse)

      Some(res)

    }

    def getAllContributors(repo: GHRepository) =
      service.getAllContributors(organizationId, repo.name).map(c => (repo, c))

    for {
      allRepositories      <- service.getAllRepositories(organizationId)
      reposAndContributors <- Future.sequence(allRepositories.map(getAllContributors))
      result = rankContributors(reposAndContributors)
    } yield result
  }

}
