package com.github.leonhardtdavid.arq2.routes

import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import io.circe.Json
import io.circe.syntax._

/**
  * Router trait for common interface.
  */
trait Router {

  /**
    * Defines the handled routes.
    * @return A [[akka.http.scaladsl.server.Route Route]]
    */
  def routes: Route

  /**
    * Creates a [[akka.http.scaladsl.model.HttpResponse]] with status 200.
    *
    * @param json The json messaje.
    * @return An instance of [[akka.http.scaladsl.model.HttpResponse]].
    */
  protected def ok(json: Json): HttpResponse =
    HttpResponse(
      status = StatusCodes.OK,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        data = ByteString(json.noSpaces)
      )
    )

  /**
    * Creates a [[akka.http.scaladsl.model.HttpResponse]] with status 201.
    *
    * @param path The relative url to the created resource.
    * @return An instance of [[akka.http.scaladsl.model.HttpResponse]].
    */
  protected def created(path: String): HttpResponse =
    HttpResponse(
      status = StatusCodes.Created,
      headers = List(Location(path)),
      entity = HttpEntity(ContentTypes.`application/json`, data = ByteString.empty)
    )

  /**
    * Creates a [[akka.http.scaladsl.model.HttpResponse]] with status 404.
    *
    * @param message The error message.
    * @return An instance of [[akka.http.scaladsl.model.HttpResponse]].
    */
  protected def notFound(message: String): HttpResponse =
    HttpResponse(
      status = StatusCodes.NotFound,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        data = ByteString(Json.obj("error" -> message.asJson).noSpaces)
      )
    )

}

/**
  * Companion object for [[com.github.leonhardtdavid.arq2.routes.Router]]
  */
object Router {

  /**
    * Message to return on unhandled errors.
    * @param message The message to return.
    */
  case class RouteError(message: String)

}
