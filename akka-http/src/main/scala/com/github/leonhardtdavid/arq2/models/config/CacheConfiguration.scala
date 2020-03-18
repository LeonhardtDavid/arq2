package com.github.leonhardtdavid.arq2.models.config

import java.time.Duration

/**
  * Cache configurations.
  *
  * @param size Cache size (max stored items).
  * @param ttl  Time to live for each key.
  */
class CacheConfiguration(val size: Long, val ttl: Duration)
