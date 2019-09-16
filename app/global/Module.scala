package global

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import javax.inject.{Named, Singleton}
import models.configurations._
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Configuration, Environment, Logger}

import scala.concurrent.ExecutionContext

/**
  * Main Guice module.
  *
  * @param environment   Play Environment.
  * @param configuration Play Configuration instance.
  */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {}

  /**
    * Provides a common logger instance for dependency injection.
    *
    * @return An instance of [[play.api.Logger]].
    */
  @Provides @Singleton
  def loggerInstance: Logger = Logger("application")

  /**
    * Provides a custom database execution context instance for dependency injection.
    *
    * @param system An [[akka.actor.ActorSystem]] instance.
    * @return An instance of [[scala.concurrent.ExecutionContext]].
    */
  @Provides @Singleton @Named(DATABASE_DISPATCHER)
  def databaseExecutionContext(system: ActorSystem): ExecutionContext =
    new CustomExecutionContext(system, DATABASE_DISPATCHER) {}

  /**
    * Provides a custom database execution context instance for dependency injection.
    *
    * @param system An [[akka.actor.ActorSystem]] instance.
    * @return An instance of [[scala.concurrent.ExecutionContext]].
    */
  @Provides @Singleton @Named(AUTHENTICATION_DISPATCHER)
  def authenticationExecutionContext(system: ActorSystem): ExecutionContext =
    new CustomExecutionContext(system, AUTHENTICATION_DISPATCHER) {}

}
