package com.igobrilhante.github.tests

import com.igobrilhante.github.zio.infraestructure.apis.github.ZGHService
import com.igobrilhante.github.zio.interfaces.algorithms.ZStreamBasedAlgorithm
import com.igobrilhante.github.zio.interfaces.controllers.Controller
import org.http4s._
import org.typelevel.ci.CIString
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.{Task, ZManaged}

trait HttpServerTest {

  val runtime: zio.Runtime[zio.ZEnv] = zio.Runtime.default

  def server(): ZManaged[Any, Throwable, Http[Task, Task]] = {

    for {
      backend <- AsyncHttpClientZioBackend.managed()
      service          = new ZGHService(backend)
      rankingAlgorithm = new ZStreamBasedAlgorithm(service)
      routes           = Controller.dsl[Task](rankingAlgorithm)
    } yield routes

  }

  def makeRequest(uri: Uri): Request[Task]#Self#Self = Request[Task](method = Method.GET, uri = uri)
    .withHeaders(
      Header.Raw(CIString("Content-Type"), "application/json")
    )

}
