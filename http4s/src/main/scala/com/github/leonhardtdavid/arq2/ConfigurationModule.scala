package com.github.leonhardtdavid.arq2

import com.github.leonhardtdavid.arq2.models.config.InterfaceConfiguration
import com.typesafe.config.ConfigFactory

/**
  * Module for dependency injection.
  */
trait ConfigurationModule {

  private val config = ConfigFactory.load()

  val interfaceConfiguration: InterfaceConfiguration = {
    new InterfaceConfiguration(
      host = this.config.getString("http.interface"),
      port = this.config.getInt("http.port")
    )
  }

}
