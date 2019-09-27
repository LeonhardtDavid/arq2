package com.github.leonhardtdavid.arq2.services

import akka.event.LoggingAdapter
import com.github.leonhardtdavid.arq2.models.Weather
import com.github.leonhardtdavid.arq2.models.config._
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import javax.inject.{Inject, Named}
import io.circe.syntax._

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
  )(implicit @Named(EXTERNAL_DISPATCHER) ec: ExecutionContext)
    extends CirceImplicits {

  private val cacheKeyPrefix = "weather-cache"

  /**
    * Get weather info.
    *
    * @param city    City name.
    * @param country Country name.
    * @param days    Days quantity.
    * @return An eventual weather info.
    */
  def get(city: String, country: String, days: Int): Future[List[Weather]] = {
    val key = s"$cacheKeyPrefix-$city-$country"

    this.cache.get(key) match {
      case Some(value) => Future.successful(io.circe.parser.parse(value).right.get.as[List[Weather]].right.get) // TODO?

      case _ =>
        this.ws
          .get(this.config.urlFor(city, country, days))
          .map(_.as[ExternalWeatherModel])
          .map {
            case Right(external) =>
              val list = external.list.map { w =>
                Weather(w.temp.day, w.pressure, w.humidity, w.weather.head.main, w.weather.head.description)
              }
              this.cache.put(key, list.asJson.noSpaces)
              list
            case Left(e) => throw e
          }
          .recover {
            case e: Throwable =>
              logger.error(e, "Error getting weather response")
              throw e
          }
    }
  }

  // scalastyle:off scaladoc
  case class ExternalWeatherModel(list: List[ExternalWeatherInfo], city: ExternalCityInfo)
  case class ExternalWeatherInfo(temp: ExternalTem, weather: List[ExternalWeather], pressure: Float, humidity: Float)
  case class ExternalTem(day: Float)
  case class ExternalWeather(main: String, description: String, icon: String)
  case class ExternalCityInfo(coord: Coordinate)
  case class Coordinate(lat: Double, lon: Double)
  // scalastyle:on scaladoc

}
