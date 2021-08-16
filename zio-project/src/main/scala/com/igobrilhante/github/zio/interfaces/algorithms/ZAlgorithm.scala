package com.igobrilhante.github.zio.interfaces.algorithms

import cats.implicits._
import com.igobrilhante.github.core.application.algorithms.RankAlgorithm
import com.igobrilhante.github.core.application.services.GHService
import com.igobrilhante.github.core.entities.{GHContributor, GHRepository}
import zio.clock.Clock
import zio.duration.durationInt
import zio.logging.log
import zio.logging.slf4j.Slf4jLogger
import zio.stream.{ZSink, ZStream}
import zio.{Chunk, Task}

/** Rank algorithm implementation using ZIO Task. */
class ZAlgorithm(service: GHService[Task]) extends RankAlgorithm[Task] {

  private type ContributorRepo = (GHContributor, GHRepository)

  private val logFormat   = "%s"
  private val loggerLayer = Slf4jLogger.make { (_, message) => logFormat.format(message) }

  override def computeRanking(organizationId: String): Task[Option[List[GHContributor]]] = {

    /* Given the list of tuples, aggregate the contributions by the contributor id */
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

      res.some
    }

    /* Create the streaming of contributors using pagination */
    def createContributorsStream(repository: GHRepository) = ZStream.paginateM(1) { page =>
      for {
        result <- service.getContributors(organizationId, repository.name, page).map(c => (repository, c))
        next = if (result._2.isEmpty) None else Some(page + 1)
      } yield (result, next)
    }

    /* Create the streaming of repositories using pagination */
    def createRepositoriesStream = {

      val repositoryPagination = ZStream.paginateChunkM(1) { page =>
        for {
          list <- service.getRepositories(organizationId, page)
          next = if (list.isEmpty) None else Some(page + 1)
        } yield (Chunk.fromArray(list.toArray), next)
      }

      for {
        _            <- ZStream.fromEffect(service.getOrganization(organizationId))
        repositories <- repositoryPagination
      } yield repositories
    }

    /* Create the sink to materialize the result into a list */
    def createSink =
      ZSink.foldLeft[ContributorRepo, List[ContributorRepo]](List.empty[ContributorRepo]) { case (result, tuple) =>
        result :+ tuple
      }

    /* Execute the computation graph */
    def executeGraph = createRepositoriesStream
      .throttleShape(100, 1.second)(_ => 1)
      .flatMap(createContributorsStream)
      .map { case (repo, contributors) => contributors.map((_, repo)) }
      .mapConcat(identity)
      .run(createSink)
      .provideLayer(Clock.live)

    val result = for {
      _            <- log.info(s"computeRanking $organizationId")
      contributors <- executeGraph
    } yield computeContributorsRank(contributors)

    result.provideLayer(loggerLayer)
  }
}
