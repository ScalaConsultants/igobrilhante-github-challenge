package com.igobrilhante.github.tests
import com.igobrilhante.github.zio.infraestructure.apis.github.ZGHService
import com.igobrilhante.github.zio.interfaces.algorithms.ZAlgorithm
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.console._

object GHServiceTest extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    (for {
      httpClient <- AsyncHttpClientZioBackend()
      service = new ZGHService(httpClient)
      ranking = new ZAlgorithm(service)
      org <- ranking.computeRanking("ScalaConsultants")
      _   <- putStr(org.toString)
    } yield ()).exitCode

  }
}
