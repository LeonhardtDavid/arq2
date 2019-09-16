package global

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.evolutions.InvalidDatabaseRevision
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.twirl.api.Html

import scala.concurrent._

/**
  * Manage unhandled errors
  *
  * @param logger A [[play.api.Logger]] instance.
  */
@Singleton
class ErrorHandler @Inject()(logger: Logger) extends HttpErrorHandler {

  private val errorKey = "error"

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful {
      val msg = s"A client error occurred: $message"
      logger.warn(msg)
      Status(statusCode)(Json.obj(errorKey -> msg))
    }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = Future.successful {
    exception match {
      case e: InvalidDatabaseRevision =>
        PreconditionRequired(Html(e.htmlDescription()))

      case _ =>
        val msg = s"A server error occurred: ${exception.getMessage}"
        logger.error(msg, exception)
        InternalServerError(Json.obj(errorKey -> msg))
    }
  }

}
