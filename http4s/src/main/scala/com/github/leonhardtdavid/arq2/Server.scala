package com.github.leonhardtdavid.arq2

import cats.effect.{ContextShift, IO, Timer}
import com.github.leonhardtdavid.arq2.models.config.InterfaceConfiguration
import com.github.leonhardtdavid.arq2.routes.{HealthcheckRouter, UsersRouter}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

/**
  * Server creator.
  *
  * @param config            Server configuration.
  * @param healthcheckRouter Healthecheck routes handler.
  * @param usersRouter       Users routes handler.
  */
class Server(config: InterfaceConfiguration, healthcheckRouter: HealthcheckRouter, usersRouter: UsersRouter) {

  /**
    * Initialize Server.
    *
    * @param timer Timer
    * @param cs    ContextShift
    * @return An FS2 Stream
    */
  def stream(implicit timer: Timer[IO], cs: ContextShift[IO]): Stream[IO, Nothing] = {
    val httpApp = Router(
      "/"    -> this.healthcheckRouter.healthcheckRoutes,
      "/api" -> this.usersRouter.usersRoutes
    ).orNotFound

    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    BlazeServerBuilder[IO]
      .bindHttp(this.config.port, this.config.host)
      .withHttpApp(finalHttpApp)
      .serve
      .drain
  }

}
