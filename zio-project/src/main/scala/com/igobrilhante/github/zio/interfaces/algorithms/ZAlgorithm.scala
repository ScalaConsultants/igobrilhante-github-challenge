package com.igobrilhante.github.zio.interfaces.algorithms

import com.igobrilhante.github.core.application.algorithms.RankAlgorithm
import com.igobrilhante.github.core.application.services.GHService
import com.igobrilhante.github.core.entities.{GHContributor, GHRepository}
import zio.clock.Clock
import zio.duration.durationInt
import zio.stream.{ZSink, ZStream}
import zio.{Chunk, Task}

class ZAlgorithm(service: GHService[Task]) extends RankAlgorithm[Task] {
  private type ContributorRepo = (GHContributor, GHRepository)
  private val MaxReposPerPage = 100

  override def computeRanking(organizationId: String): Task[Option[List[GHContributor]]] = {

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

    def contributorsStream(repository: GHRepository) = {
      ZStream.paginateM(1) { page =>
        for {
          result <- service.getContributors(organizationId, repository.name, page).map(c => (repository, c))
          next = if (result._2.isEmpty) None else Some(page + 1)
        } yield (result, next)
      }

    }

    def repositoriesStream = ZStream.paginateChunkM(1) { page =>
      for {
        list <- service.getRepositories(organizationId, page)
        next = if (list.isEmpty) None else Some(page + 1)
      } yield (Chunk.fromArray(list.toArray), next)
    }

    def sink =
      ZSink.foldLeft[ContributorRepo, List[ContributorRepo]](List.empty[ContributorRepo]) { case (result, tuple) =>
        result :+ tuple
      }

    val materializedList = repositoriesStream
      .throttleShape(1, 10.second)(_ => 1)
      .flatMap(contributorsStream)
      .map { case (repo, contributors) => contributors.map((_, repo)) }
      .mapConcat(identity)
      .run(sink)
      .provideLayer(Clock.live)

    for {
      list <- materializedList

    } yield computeContributorsRank(list)

  }
}
