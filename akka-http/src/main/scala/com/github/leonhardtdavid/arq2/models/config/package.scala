package com.github.leonhardtdavid.arq2.models

/**
  * Constants for configuration.
  */
package object config {

  val INTERFACE_HOST = "http.interface"
  val INTERFACE_PORT = "http.port"

  val JWT_KEY              = "arq2.jwt.key"
  val JWT_DURATION_MINUTES = "arq2.jwt.duration-minutes"

  val CACHE_SIZE = "arq2.cache.size"
  val CACHE_TTL  = "arq2.cache.ttl"

  val WEATHER_URL = "arq2.weather-api.url"

  val DATABASE = "arq2.database"

  final val DATABASE_DISPATCHER = "arq2.dispatchers.database" // final because is used in annotations
  final val EXTERNAL_DISPATCHER = "arq2.dispatchers.external" // final because is used in annotations

}
