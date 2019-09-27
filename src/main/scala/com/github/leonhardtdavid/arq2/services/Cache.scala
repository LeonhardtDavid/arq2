package com.github.leonhardtdavid.arq2.services

import com.github.leonhardtdavid.arq2.models.config.CacheConfiguration
import javax.inject.{Inject, Singleton}
import org.ehcache.config.builders._

/**
  * Simple in memory cache.
  *
  * @param config An instance of [[com.github.leonhardtdavid.arq2.services.Cache]].
  */
@Singleton
class Cache @Inject()(config: CacheConfiguration) {

  private val cacheName = "cache"

  private val cacheManager = CacheManagerBuilder
    .newCacheManagerBuilder()
    .withCache(
      cacheName,
      CacheConfigurationBuilder
        .newCacheConfigurationBuilder(
          classOf[String],
          classOf[String],
          ResourcePoolsBuilder.heap(this.config.size)
        )
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(this.config.ttl))
    )
    .build()
  this.cacheManager.init()

  private val cache = this.cacheManager.getCache(cacheName, classOf[String], classOf[String])

  /**
    * Insert or update a cache element.
    *
    * @param key   Key to insert or update.
    * @param value Value.
    */
  def put(key: String, value: String): Unit = this.cache.put(key, value)

  /**
    * Try to get cache content for the given key.
    *
    * @param key Key to search.
    * @return Some if exists, otherwise None.
    */
  def get(key: String): Option[String] = Option(this.cache.get(key))

}
