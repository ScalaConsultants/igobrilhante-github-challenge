package com.igobrilhante.github.zio.application.usecases

import com.igobrilhante.github.zio.application.services.GHService
import com.igobrilhante.github.zio.domain.entities.GHOrganization
import zio.Task

case class GetOrganizationByIdUseCase(service: GHService[Task]) extends UseCase[Task, String, Option[GHOrganization]] {

  override def execute(inputData: String): Task[Option[GHOrganization]] = {
    service.getOrganization(inputData)
  }

}
