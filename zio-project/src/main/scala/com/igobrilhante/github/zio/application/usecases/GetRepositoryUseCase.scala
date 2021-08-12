package com.igobrilhante.github.zio.application.usecases

import com.igobrilhante.github.zio.application.services.GHService
import com.igobrilhante.github.zio.domain.entities.GHRepository
import zio.Task

case class GetRepositoryUseCase(service: GHService[Task]) extends UseCase[Task, String, List[GHRepository]] {

  override def execute(organizationId: String): Task[List[GHRepository]] = service.getAllRepositories(organizationId)

}
