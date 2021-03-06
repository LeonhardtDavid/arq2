package com.github.leonhardtdavid.arq2.routes.events

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.entities.EventId
import com.github.leonhardtdavid.arq2.models.Event
import com.github.leonhardtdavid.arq2.routes.assistances.AssistanceRouter
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import com.github.leonhardtdavid.arq2.routes.weather.WeatherRouter
import com.github.leonhardtdavid.arq2.routes.{AuthenticationRouter, Router}
import com.github.leonhardtdavid.arq2.services.JWTService
import com.github.leonhardtdavid.arq2.services.resources.{EventResourceHandler, UserResourceHandler}
import io.circe.Json
import io.circe.syntax._
import javax.inject.Inject

import scala.concurrent.ExecutionContext

/**
  * Events router.
  *
  * @param handler          A database handler instance.
  * @param jwt              JWT token creator and validator.
  * @param userHandler      Users database handler.
  * @param weatherRouter    An instance of [[com.github.leonhardtdavid.arq2.routes.weather.WeatherRouter]].
  * @param assistanceRouter An instance of [[com.github.leonhardtdavid.arq2.routes.assistances.AssistanceRouter]].
  * @param ec               An implicit [[scala.concurrent.ExecutionContext]] instance.
  */
class EventsRouter @Inject()(
    handler: EventResourceHandler,
    val jwt: JWTService,
    userHandler: UserResourceHandler,
    weatherRouter: WeatherRouter,
    assistanceRouter: AssistanceRouter
  )(implicit ec: ExecutionContext)
    extends Router
    with AuthenticationRouter
    with CirceImplicits {

  override val routes: Route =
    authenticated { username =>
      pathPrefix("events") {
        pathEnd {
          list ~
            save(username)
        } ~
          pathPrefix(LongNumber) { id =>
            pathEnd {
              getEvent(id) ~
                update(id, username) ~
                deleteEvent(id, username)
            } ~
              this.weatherRouter.routes(EventId(id)) ~
              this.assistanceRouter.eventsRoutes(username, EventId(id))
          }
      }
    }

  private def list: Route =
    (get & parameters(('from.as[Long] ? 0L, 'quantity.as[Int] ? 10))) { (from: Long, quantity: Int) =>
      if (quantity > 1000) {
        complete(this.badRequest("maximum quantity is 1000"))
      } else {
        complete {
          val futureList  = this.handler.list(from, quantity)
          val futureCount = this.handler.count

          for {
            list  <- futureList
            count <- futureCount
          } yield
            Json.obj(
              "items" -> list.asJson,
              "count" -> count.asJson
            )
        }
      }
    }

  private def save(username: String): Route =
    (post & entity(as[Event])) { req =>
      extractRequestContext { context =>
        complete {
          for {
            Some(user) <- this.userHandler.findByUsername(username)
            event      <- this.handler.save(req.copy(id = None, owner = user.id.get))
          } yield this.created(s"${context.request.uri.toRelative.toString()}/${event.id.get.underlying}")
        }
      }
    }

  private def update(id: Long, username: String): Route = {
    val eventId = EventId(id)

    (put & entity(as[Event])) { req =>
      complete {
        for {
          Some(user)    <- this.userHandler.findByUsername(username)
          Some(dbEvent) <- this.handler.findById(eventId) if dbEvent.owner == user.id.get
          event         <- this.handler.update(req.copy(id = Some(eventId), owner = user.id.get))
        } yield this.ok(event.asJson)
      }
    }
  }

  private def getEvent(id: Long): Route =
    get {
      complete {
        this.handler.findById(EventId(id)) map {
          case Some(event) => this.ok(event.asJson)
          case _           => this.notFound(s"Event with id $id not found")
        }
      }
    }

  private def deleteEvent(id: Long, username: String): Route =
    delete {
      complete {
        for {
          Some(user) <- this.userHandler.findByUsername(username)
          eventId = EventId(id)
          Some(event) <- this.handler.findById(eventId) if user.id.get == event.owner // TODO fix
        } yield
          this.handler.delete(eventId) map { ok =>
            if (ok) this.noContent
            else this.notFound("Event not found")
          }
      }
    }

}
