package com.github.leonhardtdavid.arq2.models

/**
  * Constants for configuration.
  */
package object config {

  val INTERFACE_HOST = "http.interface"
  val INTERFACE_PORT = "http.port"

  val JWT_KEY              = "arq2.jwt.key"
  val JWT_DURATION_MINUTES = "arq2.jwt.duration-minutes"

  val DATABASE = "arq2.database"

  final val DATABASE_DISPATCHER = "arq2.dispatchers.database" // final because is used in annotations

}
