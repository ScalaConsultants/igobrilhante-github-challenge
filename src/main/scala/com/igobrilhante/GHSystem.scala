package com.igobrilhante

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration

import akka.actor.typed.{ActorSystem, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors

import com.igobrilhante.github.api.GHService
import com.igobrilhante.github.impl.GHServiceImpl

object GHSystem {

  trait Command

  def apply(): Behavior[GHSystem.Command] = Behaviors.empty

  def main(args: Array[String]): Unit = {

    implicit val actorSystem: ActorSystem[Command] = ActorSystem.create(GHSystem(), "main")
    implicit val ec: ExecutionContextExecutor = actorSystem.dispatchers.lookup(DispatcherSelector.fromConfig("app.service-dispatcher"))

    val service: GHService = new GHServiceImpl()

    val res = Await.result(service.getRankedContributors("ScalaConsultants"), Duration.Inf)

    println(res.take(10).mkString("\n"))

    actorSystem.terminate()

    sys.exit(0)
  }

}
