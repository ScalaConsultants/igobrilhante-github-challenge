package com.igobrilhante.github.tests
import com.igobrilhante.github.zio.infraestructure.apis.github.future.GHServiceTask
import zio._
import zio.console._

object GHServiceTest extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val service = new GHServiceTask()

    (for {
      org <- service.getOrganization("ScalaConsultant").catchAll(_ => Task.succeed(None))
      _   <- putStr(org.toString)
    } yield ()).exitCode

  }
}
