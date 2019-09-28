package com.github.leonhardtdavid.arq2.routes.weather

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.entities.EventId
import com.github.leonhardtdavid.arq2.routes.ResultsHelper
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import com.github.leonhardtdavid.arq2.services.WeatherService
import com.github.leonhardtdavid.arq2.services.resources.EventResourceHandler
import io.circe.Json
import io.circe.syntax._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

/**
  * Weather router.
  *
  * @param weather     A [[com.github.leonhardtdavid.arq2.services.WeatherService]] instance.
  * @param evenHandler A [[com.github.leonhardtdavid.arq2.services.resources.EventResourceHandler]] instance.
  * @param ec          An implicit [[scala.concurrent.ExecutionContext]].
  */
class WeatherRouter @Inject()(weather: WeatherService, evenHandler: EventResourceHandler)(implicit ec: ExecutionContext)
    extends ResultsHelper
    with CirceImplicits {

  /**
    * Defines the handled routes.
    *
    * @param eventId An event id.
    * @return A [[akka.http.scaladsl.server.Route Route]]
    */
  def routes(eventId: EventId): Route =
    path("weather") {
      get {
        complete {
          this.evenHandler.findById(eventId) flatMap {
            case Some(event) =>
              val diff = OffsetDateTime.now.until(event.date, ChronoUnit.DAYS)

              if (diff > 0 && diff <= 16) { // TODO Add config
                this.weather.get(event.venue.city, event.venue.country, diff.toInt)
              } else {
                Future.successful(Nil)
              }
            case _ =>
              Future.successful(Nil)
          } map { list =>
            this.ok(Json.obj("info" -> list.asJson))
          }
        }
      }
    }

}
