package com.github.leonhardtdavid.arq2.routes.weather

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.entities.EventId
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits

/**
  * Weather router.
  */
class WeatherRouter extends CirceImplicits {

  /**
    * Defines the handled routes.
    *
    * @param eventId An event id.
    * @return A [[akka.http.scaladsl.server.Route Route]]
    */
  def routes(eventId: EventId): Route = {
    val _ = eventId // TODO REMOVE

    path("weather") {
      get {
        complete {}
      }
    }
  }

}
