package com.igobrilhante.github.interfaces.gateway

import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler

import com.igobrilhante.github.domain.entities.{NotFoundException, UnexpectedErrorException}
import io.circe.syntax.EncoderOps

object AppExceptionHandler {

  def apply(): ExceptionHandler =
    ExceptionHandler {
      case e: NotFoundException =>
        extractUri { uri =>
          complete(
            HttpResponse(
              NotFound,
              entity = HttpEntity(ContentTypes.`application/json`, e.asJson.dropNullValues.noSpaces)
            )
          )
        }
      case e: UnexpectedErrorException =>
        extractUri { uri =>
          complete(
            HttpResponse(
              InternalServerError,
              entity = HttpEntity(ContentTypes.`application/json`, e.asJson.dropNullValues.noSpaces)
            )
          )
        }
      case e: Throwable =>
        extractUri { uri =>
          complete(
            HttpResponse(
              InternalServerError,
              entity = HttpEntity(
                ContentTypes.`application/json`,
                UnexpectedErrorException.withDetail(e.getMessage).asJson.dropNullValues.noSpaces
              )
            )
          )
        }
    }

}
