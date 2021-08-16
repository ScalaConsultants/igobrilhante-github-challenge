package com.igobrilhante.github.tests
import com.igobrilhante.github.zio.infraestructure.apis.github.ZGHService
import com.igobrilhante.github.zio.interfaces.algorithms.ZAlgorithm
import org.slf4j.LoggerFactory
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.clock.Clock
import zio.console._
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, log}

object GHServiceTest extends zio.App {

  val logger = LoggerFactory.getLogger("teste")

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

import zio.logging._

object Slf4jAndCorrelationId extends zio.App {
  val logFormat = "[correlation-id = %s] %s"
  val logger    = LoggerFactory.getLogger("teste")

  val env: ULayer[Logging] =
    Slf4jLogger.make { (context, message) =>
      val correlationId = LogAnnotation.CorrelationId.render(
        context.get(LogAnnotation.CorrelationId)
      )
      logFormat.format(correlationId, message)
    }

  def generateCorrelationId =
    Some(java.util.UUID.randomUUID())

  override def run(args: List[String]) = {
    (for {
      _ <- URIO.effectTotal(logger.info("teste"))
      r <- log.info("Hello from ZIO logger")
    } yield r)
      .provideCustomLayer(env)
      .as(ExitCode.success)

  }

}

object Simple extends zio.App {

  val env: ZLayer[Console with Clock, Nothing, Logging] =
    Logging.console(
      logLevel = LogLevel.Info,
      format = LogFormat.ColoredLogFormat()
    ) >>> Logging.withRootLoggerName("my-component")

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    log.info("Hello from ZIO logger").provideCustomLayer(env).as(ExitCode.success)
}
