package com.github.leonhardtdavid.arq2.routes

import cats.effect.IO
import com.github.leonhardtdavid.arq2.info.BuildInfo
import org.http4s.HttpRoutes
import org.http4s.MediaType._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

/**
  * Health check routes handler.
  */
class HealthcheckRouter {

  /**
    * Creates the health check route.
    *
    * @return A [[org.http4s.HttpRoutes]].
    */
  def healthcheckRoutes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}

    import dsl._

    HttpRoutes.of[IO] {
      case GET -> Root / "healthcheck" =>
        Ok(BuildInfo.toJson, `Content-Type`(application.json))
    }
  }

}
