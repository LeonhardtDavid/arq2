package controllers.circe

import cats.data.NonEmptyList
import io.circe._
import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import models.json.CirceImplicits
import play.api.libs.circe.Circe
import play.api.mvc.{BaseController, BodyParser, Result}

import scala.concurrent.ExecutionContext

/**
  * Custom body parser for validations
  */
trait CirceBodyParser extends Circe with CirceImplicits { _: BaseController =>

  /**
    * Error representation to use in responses.
    *
    * @param error   Error type.
    * @param path    Property with the error.
    * @param message Error message.
    */
  case class ValidationError(error: String, path: String, message: String)
  implicit protected val validationErrorEncoder: Encoder[ValidationError] = deriveEncoder

  /**
    * Body parser for json applying validations.
    *
    * @param ec An implicit execution context.
    * @tparam T A circe decoder.
    * @return A body parser.
    */
  protected def decode[T: Decoder](implicit ec: ExecutionContext): BodyParser[T] =
    circe.json.validate(decodeJsonValue[T])

  private def decodeJsonValue[T: Decoder](json: Json): Either[Result, T] =
    implicitly[Decoder[T]]
      .accumulating(json.hcursor)
      .leftMap(ex => BadRequest(validationErrorsAsInvalidRequestJson(decodingFailuresAsValidationError(ex))))
      .toEither

  private def decodingFailuresAsValidationError(
      decodingFailures: NonEmptyList[DecodingFailure]
    ): List[ValidationError] = {
    val validationErrors: NonEmptyList[ValidationError] = decodingFailures map { failure =>
      val possiblePath = CursorOp.opsToPath(failure.history)
      val path: String = if (failure.history.isEmpty) possiblePath else possiblePath.substring(1)
      failure.message match {
        case "Attempt to decode value on failed cursor" =>
          ValidationError("required", path, "The specified parameter wasn't present in the request.")

        case _ =>
          ValidationError("invalid", path, failure.message)
      }
    }
    validationErrors.toList
  }

  private def validationErrorsAsInvalidRequestJson(validationErrors: List[ValidationError]): Json =
    Json.obj("errors" -> validationErrors.asJson)

}
