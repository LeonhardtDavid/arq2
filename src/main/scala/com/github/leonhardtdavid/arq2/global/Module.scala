package com.github.leonhardtdavid.arq2.global

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import com.github.leonhardtdavid.arq2.models.config._
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.ConfigFactory
import javax.inject.{Named, Singleton}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Main Guice module
  *
  * @param logger A [[akka.event.LoggingAdapter]] instance.
  * @param system A [[akka.actor.ActorSystem]] instance.
  */
class Module(logger: LoggingAdapter, system: ActorSystem) extends AbstractModule {

  private val config = ConfigFactory.load()

  override def configure(): Unit = bind(classOf[LoggingAdapter]).toInstance(this.logger)

  /**
    * Bind a [[com.github.leonhardtdavid.arq2.models.config.InterfaceConfiguration]] instance for dependency injection.
    *
    * @return A [[com.github.leonhardtdavid.arq2.models.config.InterfaceConfiguration]] instance.
    */
  @Provides @Singleton
  def interfaceConfiguration: InterfaceConfiguration = new InterfaceConfiguration(
    host = this.config.getString(INTERFACE_HOST),
    port = this.config.getInt(INTERFACE_PORT)
  )

  /**
    * Bind a [[com.github.leonhardtdavid.arq2.models.config.JWTConfiguration]] instance for dependency injection.
    *
    * @return A [[com.github.leonhardtdavid.arq2.models.config.JWTConfiguration]] instance.
    */
  @Provides @Singleton
  def jwtConfiguration: JWTConfiguration = new JWTConfiguration(
    key = this.config.getString(JWT_KEY),
    duration = this.config.getInt(JWT_DURATION_MINUTES).minutes
  )

  /**
    * Bind a [[com.github.leonhardtdavid.arq2.models.config.CacheConfiguration]] instance for dependency injection.
    *
    * @return A [[com.github.leonhardtdavid.arq2.models.config.CacheConfiguration]] instance.
    */
  @Provides @Singleton
  def cacheConfiguration: CacheConfiguration = new CacheConfiguration(
    size = this.config.getLong(CACHE_SIZE),
    ttl = this.config.getDuration(CACHE_TTL)
  )

  /**
    * Bind a [[com.github.leonhardtdavid.arq2.models.config.WeatherConfiguration]] instance for dependency injection.
    *
    * @return A [[com.github.leonhardtdavid.arq2.models.config.WeatherConfiguration]] instance.
    */
  @Provides @Singleton
  def weatherConfiguration: WeatherConfiguration = new WeatherConfiguration(
    url = this.config.getString(WEATHER_URL)
  )

  /**
    * Bind a [[slick.basic.DatabaseConfig]] instance for dependency injection.
    *
    * @return A [[slick.basic.DatabaseConfig]] instance.
    */
  @Provides @Singleton
  def database: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig(DATABASE, this.config)

  /**
    * Bind a [[scala.concurrent.ExecutionContext]] default instancefor dependency injection.
    *
    * @return The default [[scala.concurrent.ExecutionContext]] instance for database integration.
    */
  @Provides @Singleton
  def defaultExecutionContext: ExecutionContext = this.system.dispatcher

  /**
    * Bind a [[scala.concurrent.ExecutionContext]] instance for database integration for dependency injection.
    *
    * @return A [[scala.concurrent.ExecutionContext]] instance for database integration.
    */
  @Provides @Singleton @Named(DATABASE_DISPATCHER)
  def databaseExecutionContext: ExecutionContext = this.system.dispatchers.lookup(DATABASE_DISPATCHER)

  /**
    * Bind a [[scala.concurrent.ExecutionContext]] instance for external services integration for dependency injection.
    *
    * @return A [[scala.concurrent.ExecutionContext]] instance for external services integration.
    */
  @Provides @Singleton @Named(EXTERNAL_DISPATCHER)
  def externalExecutionContext: ExecutionContext = this.system.dispatchers.lookup(EXTERNAL_DISPATCHER)

}
