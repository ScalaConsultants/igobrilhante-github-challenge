package com.igobrilhante.github.interfaces.algorithms

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}

import cats.data.OptionT
import com.igobrilhante.github.application.algorithms.RankAlgorithm
import com.igobrilhante.github.application.services.GHService
import com.igobrilhante.github.domain.entities.{GHContributor, GHRepository}

class StreamingRankAlgorithm(service: GHService)(implicit system: ActorSystem[_], ec: ExecutionContext)
    extends RankAlgorithm[Future] {
  private type ContributorRepo = (GHContributor, GHRepository)
  private val MaxReposPerPage = 100

  override def computeRanking(organizationId: String): Future[Option[List[GHContributor]]] = {

    def getContributorsWithRepo(repository: GHRepository) = {
      service.getAllContributors(organizationId, repository.name).map(c => (repository, c))
    }

    def computeContributorsRank(list: List[ContributorRepo]) = {
      val res = list
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

    def source(pages: Int) = Source.fromIterator(() => (1 to pages).iterator)

    def sink =
      Sink.fold[List[ContributorRepo], ContributorRepo](List.empty[ContributorRepo]) { case (result, tuple) =>
        result :+ tuple
      }

    val futureOptSource = for {
      org <- OptionT(service.getOrganization(organizationId))
      pages           = getNumberOfPages(org.total, MaxReposPerPage)
      repoPagesSource = source(pages)
    } yield repoPagesSource

    val futureSource = futureOptSource.value.map(_.getOrElse(Source.empty[Int]))

    def futureList = Source
      .futureSource(futureSource)
      .mapAsyncUnordered(2) {
        service.getRepositories(organizationId, _)
      }
      .mapConcat(identity)
      .throttle(50, 1.second)
      .mapAsync(4)(getContributorsWithRepo)
      .map { case (repo, contributors) => contributors.map((_, repo)) }
      .mapConcat(identity)
      .toMat(sink)(Keep.right)
      .run()

    for {
      list <- futureList
      rankedList = computeContributorsRank(list)
    } yield rankedList
  }

  private def getNumberOfPages(total: Int, maxPerPage: Int): Int = math.ceil(total / maxPerPage.toDouble).toInt

}
