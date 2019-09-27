package com.github.leonhardtdavid.arq2.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.github.leonhardtdavid.arq2.models.config.EXTERNAL_DISPATCHER
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import io.circe.Json
import javax.inject.{Inject, Named}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Make requests to rest external serices.
  *
  * @param materializer An implicit [[akka.stream.Materializer]].
  * @param system       An implicit [[akka.actor.ActorSystem]].
  * @param executor     An implicit [[scala.concurrent.ExecutionContext]].
  */
class WSClient @Inject()(
    implicit
    materializer: Materializer,
    system: ActorSystem,
    @Named(EXTERNAL_DISPATCHER) executor: ExecutionContext)
    extends CirceImplicits {

  private def getResponse(uri: String, method: HttpMethod, headers: List[HttpHeader], optionalJson: Option[Json]) = {
    val data = optionalJson
      .map(json => HttpEntity(ContentTypes.`application/json`, json.toString()))
      .getOrElse(HttpEntity.Empty)

    Http().singleRequest(HttpRequest(method, uri, headers, data))
  }

  /**
    * Call a rest api with GET method.
    *
    * @param url Url to make the request.
    * @return A [[scala.concurrent.Future]] with an eventual response.
    */
  def get(url: String): Future[Json] =
    this
      .getResponse(url, HttpMethods.GET, Nil, None)
      .filter(_.status.isSuccess())
      .flatMap { response =>
        Unmarshal(response.entity).to[Json]
      }

}
