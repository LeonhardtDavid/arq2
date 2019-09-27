package com.github.leonhardtdavid.arq2.services

import akka.event.LoggingAdapter
import com.github.leonhardtdavid.arq2.models.config._
import io.circe.Json
import javax.inject.{Inject, Named}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Weather service to access to http://api.openweathermap.org.
  *
  * @param logger Logger instance.
  * @param cache  A cache instance.
  * @param ws     A rest client instance.
  * @param config A [[com.github.leonhardtdavid.arq2.models.config.WeatherConfiguration]] instance.
  * @param ec     An implicit [[scala.concurrent.ExecutionContext]].
  */
class WeatherService @Inject()(
    logger: LoggingAdapter,
    cache: Cache,
    ws: WSClient,
    config: WeatherConfiguration
  )(implicit @Named(EXTERNAL_DISPATCHER) ec: ExecutionContext) {

  private val cacheKeyPrefix = "weather-cache"

  /**
    * Get weather info.
    *
    * @param city    City name.
    * @param country Country name.
    * @param days    Days quantity.
    * @return An eventual weather info.
    */
  def get(city: String, country: String, days: Int): Future[Json] = {
    val key = s"$cacheKeyPrefix-$city-$country"

    this.cache.get(key) match {
      case Some(value) =>
        Future.successful(io.circe.parser.parse(value).getOrElse(Json.obj()))

      case _ =>
        this.ws
          .get(this.config.urlFor(city, country, days))
          .map { json =>
            this.cache.put(key, json.noSpaces)
            json
          }
          .recover {
            case e: Throwable =>
              logger.error(e, "Error getting weather response")
              Json.obj()
          }
    }
  }

}
