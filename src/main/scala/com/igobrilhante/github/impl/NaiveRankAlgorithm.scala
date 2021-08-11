package com.igobrilhante.github.impl

import scala.concurrent.{ExecutionContext, Future}

import com.igobrilhante.github.api.{GHService, RankAlgorithm}
import com.igobrilhante.github.commons.Logging
import com.igobrilhante.github.models.{GHContributor, GHRepository}

class NaiveRankAlgorithm(service: GHService)(implicit ec: ExecutionContext) extends RankAlgorithm with Logging {

  override def computeRanking(organizationId: String): Future[Option[List[GHContributor]]] = {
    logger.debug("getRankedContributors for org {}", organizationId)

    def rankContributors(list: List[(GHRepository, List[GHContributor])]) = {
      logger.debug("rankContributors for org {}", organizationId)
      val res = list
        .flatMap { case (repo, contributors) => contributors.map((_, repo)) }
        .groupBy(_._1.login)
        .map { case (_, contributorRepositories) =>
          val totalContributions = contributorRepositories.map(_._1.contributions).sum
          val contributor = contributorRepositories.head._1
          contributor.copy(contributions = totalContributions)
        }
        .toList
        .sortBy(_.contributions)(Ordering[Int].reverse)

      Some(res)

    }

    def getAllContributors(repo: GHRepository) =
      service.getAllContributors(organizationId, repo.name).map(c => (repo, c))

    for {
      allRepositories <- service.getAllRepositories(organizationId)
      reposAndContributors <- Future.sequence(allRepositories.map(getAllContributors))
      result = rankContributors(reposAndContributors)
    } yield result
  }

}
