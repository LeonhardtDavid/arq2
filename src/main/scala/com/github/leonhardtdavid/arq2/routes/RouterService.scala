package com.github.leonhardtdavid.arq2.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.github.leonhardtdavid.arq2.routes.Router._
import com.github.leonhardtdavid.arq2.routes.healthcheck.HealthcheckRouter
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import com.github.leonhardtdavid.arq2.routes.users.UsersRouter
import io.circe.syntax._
import javax.inject.{Inject, Singleton}

/**
  * Routes handler.
  *
  * @param logger            Logger instance.
  * @param healthCheckRouter A [[com.github.leonhardtdavid.arq2.routes.healthcheck.HealthcheckRouter]] instance.
  * @param usersRouter       A [[com.github.leonhardtdavid.arq2.routes.users.UsersRouter]] instance.
  */
@Singleton
class RouterService @Inject()(logger: LoggingAdapter, healthCheckRouter: HealthcheckRouter, usersRouter: UsersRouter)
    extends CirceImplicits {

  private def getFailureJson(message: String) =
    HttpEntity(
      ContentTypes.`application/json`,
      RouteError(message).asJson.noSpaces
    )

  private val exceptionHandler = ExceptionHandler {
    case e =>
      extractUri { uri =>
        logger.error(e, "Request to {} could not be handled normally", uri)
        complete {
          HttpResponse(InternalServerError, entity = this.getFailureJson(e.toString))
        }
      }
  }

  private def badRequestHandler(msg: String) = {
    logger.info("bad request: {}", msg)
    complete(HttpResponse(BadRequest, entity = this.getFailureJson(s"Invalid request: $msg")))
  }

  /**
    * Custom Error handler.
    * @return A [[akka.http.scaladsl.server.RejectionHandler]] instance.
    */
  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          complete(HttpResponse(Forbidden, entity = this.getFailureJson("You don't have access")))
      }
      .handle {
        case ValidationRejection(msg, _)              => badRequestHandler(msg)
        case MalformedRequestContentRejection(msg, _) => badRequestHandler(msg)
      }
      .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        complete(
          HttpResponse(
            MethodNotAllowed,
            entity = this.getFailureJson(s"Unsupported method! Supported: ${names mkString " or "}!")
          ))
      }
      .handleNotFound {
        complete(HttpResponse(NotFound, entity = this.getFailureJson("Nothing here!")))
      }
      .result()

  val routes: Route = handleExceptions(exceptionHandler) {
    healthCheckRouter.routes ~
      pathPrefix("api") {
        usersRouter.routes
      }
  }

}
