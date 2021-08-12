package com.igobrilhante.github.application.usecases

import scala.concurrent.Future

import com.igobrilhante.github.application.services.GHService
import com.igobrilhante.github.domain.entities.GHOrganization

case class GetOrganizationByIdUseCase(service: GHService) extends UseCaseFuture[String, Option[GHOrganization]] {

  override def execute(inputData: String): Future[Option[GHOrganization]] = {
    service.getOrganization(inputData)
  }

}
