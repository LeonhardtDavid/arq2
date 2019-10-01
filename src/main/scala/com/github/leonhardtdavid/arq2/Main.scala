package com.github.leonhardtdavid.arq2

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RejectionHandler
import akka.stream.{ActorMaterializer, Materializer}
import com.github.leonhardtdavid.arq2.global.Module
import com.github.leonhardtdavid.arq2.models.config.InterfaceConfiguration
import com.github.leonhardtdavid.arq2.routes.RouterService
import com.google.inject.Guice
import kamon.Kamon

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Main entry for application.
  */
object Main extends App {

  Kamon.init()

  implicit private val system: ActorSystem        = ActorSystem()
  implicit private val executor: ExecutionContext = system.dispatcher
  implicit private val materializer: Materializer = ActorMaterializer()

  private val logger = Logging(system.eventStream, "application")

  private val injector = Guice.createInjector(new Module(logger, system, materializer))

  private val config = injector.getInstance(classOf[InterfaceConfiguration])

  private val service = injector.getInstance(classOf[RouterService])

  implicit private def rejectHandler: RejectionHandler = service.rejectionHandler

  Http().bindAndHandle(this.service.routes, config.host, config.port) onComplete {
    case Success(_) => logger.info("APP started on {}:{}", config.host, config.port)
    case Failure(e) => logger.error(e, "Failed to bind to {}:{}!", config.host, config.port)
  }

}
