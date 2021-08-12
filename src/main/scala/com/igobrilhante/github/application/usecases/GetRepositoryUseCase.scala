package com.igobrilhante.github.application.usecases

import scala.concurrent.Future

import com.igobrilhante.github.application.services.GHService
import com.igobrilhante.github.domain.entities.GHRepository

case class GetRepositoryUseCase(service: GHService) extends UseCaseFuture[String, List[GHRepository]] {

  override def execute(organizationId: String): Future[List[GHRepository]] = service.getAllRepositories(organizationId)

}
