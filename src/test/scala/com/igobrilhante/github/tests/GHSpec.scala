package com.igobrilhante.github.tests

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest

import com.igobrilhante.GHSystem
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

  val timeout              = Timeout(30 seconds)
  val testKit              = ActorTestKit()
  implicit val ec          = testKit.system.executionContext
  implicit val actorSystem = ActorSystem.create(GHSystem(), "main")

  override def afterAll(): Unit = {
    actorSystem.terminate()
    testKit.shutdownTestKit()
  }

}
