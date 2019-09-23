package com.github.leonhardtdavid.arq2.routes.events

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.entities.EventId
import com.github.leonhardtdavid.arq2.models.Event
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import com.github.leonhardtdavid.arq2.routes.{AuthenticationRouter, Router}
import com.github.leonhardtdavid.arq2.services.resources.{EventResourceHandler, UserResourceHandler}
import com.github.leonhardtdavid.arq2.services.tokens.JWTService
import io.circe.Json
import io.circe.syntax._
import javax.inject.Inject

import scala.concurrent.ExecutionContext

/**
  * Events router.
  *
  * @param handler     A database handler instance.
  * @param jwt         JWT token creator and validator.
  * @param userHandler Users database handler.
  * @param ec          An implicit [[scala.concurrent.ExecutionContext]] instance.
  */
class EventsRouter @Inject()(
    handler: EventResourceHandler,
    val jwt: JWTService,
    userHandler: UserResourceHandler
  )(implicit ec: ExecutionContext)
    extends Router
    with AuthenticationRouter
    with CirceImplicits {

  override val routes: Route =
    authenticated { username =>
      pathPrefix("events") {
        pathEnd {
          get {
            complete {
              val futureList  = this.handler.list
              val futureCount = this.handler.count

              for {
                list  <- futureList
                count <- futureCount
              } yield
                Json.obj(
                  "items"    -> list.asJson,
                  "quantity" -> count.asJson
                )
            }
          } ~
            ((post | put) & entity(as[Event])) { req =>
              extractRequestContext { context =>
                complete {
                  for {
                    Some(user) <- this.userHandler.findByUsername(username)
                    event      <- this.handler.save(req.copy(id = None, owner = user.id.get))
                  } yield this.created(s"${context.request.uri.toString()}/${event.id.get.underlying}")
                }
              }
            }
        } ~
          path(LongNumber) { id =>
            get {
              complete {
                this.handler.findById(EventId(id)) map {
                  case Some(event) => this.ok(event.asJson)
                  case _           => this.notFound(s"Event with id $id not found")
                }
              }
            } ~
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
      }
    }

}
