package com.igobrilhante.github.zio.application.usecases

import scala.concurrent.Future

trait UseCase[F[_], InputData, OutputData] {

  def execute(inputData: InputData): F[OutputData]

}

trait UseCaseFuture[I, O] extends UseCase[Future, I, O]
