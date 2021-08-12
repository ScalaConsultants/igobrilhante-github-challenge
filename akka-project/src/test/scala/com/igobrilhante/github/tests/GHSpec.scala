package com.igobrilhante.github.tests

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}

import com.igobrilhante.github.infraestructure.apis.github.future.GHServiceImpl
import com.igobrilhante.github.infraestructure.http.GHSystem
import com.igobrilhante.github.interfaces.algorithms.StreamingRankAlgorithm
import com.igobrilhante.github.interfaces.controllers.Modules
import com.igobrilhante.github.interfaces.gateway.Gateway
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

trait GHSpec
    extends AsyncFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with ScalaFutures
    with ScalatestRouteTest {

  val timeout: Timeout                          = Timeout(30 seconds)
  implicit val requestTimeout: RouteTestTimeout = RouteTestTimeout(120.seconds)

  val testKit: ActorTestKit                               = ActorTestKit()
  implicit val ec: ExecutionContextExecutor               = testKit.system.executionContext
  implicit val actorSystem: ActorSystem[GHSystem.Command] = ActorSystem.create(GHSystem(), "main")

  val service   = new GHServiceImpl()
  val algorithm = new StreamingRankAlgorithm(service)

  val routes: Route = Gateway(Modules(service, algorithm))

  override def afterAll(): Unit = {
    actorSystem.terminate()
    testKit.shutdownTestKit()
  }

}
