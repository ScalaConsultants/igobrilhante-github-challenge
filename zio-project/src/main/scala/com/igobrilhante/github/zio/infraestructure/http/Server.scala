package com.igobrilhante.github.zio.infraestructure.http

import com.igobrilhante.github.zio.infraestructure.apis.github.ZGHService
import com.igobrilhante.github.zio.interfaces.algorithms.ZStreamBasedAlgorithm
import com.igobrilhante.github.zio.interfaces.controllers.Controller
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.{Task, URIO, ZIO}

object Server extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, zio.ExitCode] = {
    (for {
      backend <- AsyncHttpClientZioBackend.managed()
      service         = new ZGHService(backend)
      rankingAlgorithm = new ZStreamBasedAlgorithm(service)
      routes          = Controller.dsl[Task](rankingAlgorithm)
      appServer <- AppServer.dsl().server[Task](routes).toManagedZIO
    } yield appServer).use { _ => ZIO.never }.exitCode

  }
}
