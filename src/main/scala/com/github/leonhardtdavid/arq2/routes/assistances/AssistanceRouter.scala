package com.github.leonhardtdavid.arq2.routes.assistances

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.entities.{EventId, UserId}
import com.github.leonhardtdavid.arq2.models.Assistance
import com.github.leonhardtdavid.arq2.routes.ResultsHelper
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import com.github.leonhardtdavid.arq2.services.resources.{EventResourceHandler, UserResourceHandler}
import io.circe.Json
import io.circe.syntax._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

/**
  * Assistance router.
  *
  * @param evenHandler A [[com.github.leonhardtdavid.arq2.services.resources.EventResourceHandler]] instance.
  * @param userHandler A [[com.github.leonhardtdavid.arq2.services.resources.UserResourceHandler]] instance.
  * @param ec          An implicit [[scala.concurrent.ExecutionContext]].
  */
class AssistanceRouter @Inject()(
    evenHandler: EventResourceHandler,
    userHandler: UserResourceHandler
  )(implicit ec: ExecutionContext)
    extends ResultsHelper
    with CirceImplicits {

  /**
    * Defines the handled routes for events.
    *
    * @param username  A user id.
    * @param eventId An event id.
    * @return A [[akka.http.scaladsl.server.Route Route]]
    */
  def eventsRoutes(username: String, eventId: EventId): Route =
    this.routeHelper {
      listEventAssistances(eventId) ~
        createAssistance(username, eventId)
    }

  /**
    * Defines the handled routes for users.
    *
    * @param username Logged user.
    * @param userId   A user id.
    * @return A [[akka.http.scaladsl.server.Route Route]]
    */
  def usersRoutes(username: String, userId: UserId): Route =
    this.routeHelper {
      listEventAssistancesForUser(username, userId)
    }

  private def routeHelper(route: Route) = path("assistances")(route)

  private def createAssistance(username: String, eventId: EventId): Route =
    (post & entity(as[Assistance])) { assistance =>
      extractRequestContext { context =>
        complete {
          this.evenHandler.findById(eventId) flatMap {
            case None => Future.successful(this.notFound("Invalid event"))

            case Some(event) =>
              for {
                Some(user)      <- this.userHandler.findByUsername(username)
                maybeAssistance <- this.evenHandler.assist(user.id.get, event, assistance)
              } yield
                maybeAssistance match {
                  case Some(updated) =>
                    this.created(s"${context.request.uri.toRelative.toString()}/${updated.id.get.underlying}")

                  case _ =>
                    this.badRequest("The event is full")
                }
          }
        }
      }
    }

  private def listEventAssistances(eventId: EventId): Route =
    get {
      complete {
        for {
          count <- this.evenHandler.countAssistancesForEvent(eventId)
          list  <- this.evenHandler.listAssistances(eventId)
        } yield this.createJsonResponse(list, count)
      }
    }

  private def listEventAssistancesForUser(username: String, userId: UserId): Route =
    get {
      complete {
        this.userHandler.findById(userId) flatMap {
          case Some(user) if (user.username == username) =>
            val userId = user.id.get

            for {
              count <- this.evenHandler.countAssistancesForUser(userId)
              list  <- this.evenHandler.listAssistancesForUser(userId)
            } yield this.ok(this.createJsonResponse(list, count))

          case _ =>
            Future.successful(this.notFound("Not assistances found"))
        }
      }
    }

  private def createJsonResponse(list: scala.List[Assistance], count: Int) =
    Json.obj(
      "items" -> list.asJson,
      "count" -> count.asJson
    )

}
