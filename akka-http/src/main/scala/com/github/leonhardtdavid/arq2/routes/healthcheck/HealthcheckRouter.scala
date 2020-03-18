package com.github.leonhardtdavid.arq2.routes.healthcheck

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.info.BuildInfo
import com.github.leonhardtdavid.arq2.routes.Router

/**
  * Health check router.
  */
class HealthcheckRouter extends Router {

  override val routes: Route =
    pathPrefix("healthcheck") {
      complete {
        HttpResponse(
          status = OK,
          entity = HttpEntity(ContentTypes.`application/json`, BuildInfo.toJson)
        )
      }
    }

}
