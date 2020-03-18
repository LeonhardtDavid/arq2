package com.github.leonhardtdavid.arq2

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.github.leonhardtdavid.arq2.routes.{HealthcheckRouter, UsersRouter}
import com.softwaremill.macwire._

/**
  * Main entry for application.
  */
object Main extends IOApp with ConfigurationModule {

  val healthcheckRouter: HealthcheckRouter = wire[HealthcheckRouter]
  val usersRouter: UsersRouter             = wire[UsersRouter]
  val server: Server                       = wire[Server]

  override def run(args: List[String]): IO[ExitCode] =
    this.server.stream.compile.drain.as(ExitCode.Success)

}
