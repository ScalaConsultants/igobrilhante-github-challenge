package com.igobrilhante.github.tests

import org.http4s._
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HttpServiceSpec extends AnyFlatSpec with HttpServerTest with Matchers with BeforeAndAfterAll {

  "Http Server Spec" should "return 404 for an invalid organization" in {
    val endpoint = uri"/org/adasd/contributors"
    val request  = makeRequest(endpoint)
    val response = runtime.unsafeRun(server().use { routes => routes(request) })
    response.status shouldEqual Status.NotFound
  }

  it should "get response for small organizations" in {
    val endpoint = uri"/org/InsightLab/contributors"
    val request  = makeRequest(endpoint)
    val response = runtime.unsafeRun(server().use { routes => routes(request) })

    response.status shouldEqual Status.Ok
  }

}
