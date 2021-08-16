package com.igobrilhante.akka.tests

import akka.http.scaladsl.model.StatusCodes

class HttpServerSpec extends GHSpec {

  "Http Server Spec" should "return 404 for an invalid organization" in {
    val uri = "/org/adasd/contributors"
    Get(uri) ~> routes ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "get response for small organizations" in {
    val uri = "/org/InsightLab/contributors"
    Get(uri) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

//  it should "get response for very large organizations like Apache" in {
//    val uri = "/org/Apache/contributors"
//    Get(uri) ~> routes ~> check {
//      status shouldEqual StatusCodes.OK
//    }
//  }

}
