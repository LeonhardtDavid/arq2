package com.github.leonhardtdavid.arq2.models.config

import scala.concurrent.duration.FiniteDuration

/**
  * Configuration class for JWT settings.
  *
  * @param key      A key to sign tokens.
  * @param duration How much time the token is valid.
  */
class JWTConfiguration(val key: String, val duration: FiniteDuration)
