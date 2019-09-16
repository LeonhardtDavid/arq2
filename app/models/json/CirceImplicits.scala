package models.json

import java.time.OffsetDateTime
import java.util.Currency

import entities.UserId
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import io.circe.generic.extras.{AutoDerivation, Configuration}
import io.circe.java8.time.{decodeOffsetDateTime, encodeOffsetDateTime}
import io.circe.{Decoder, Encoder}

import scala.util.Try

/**
  * Circe implicits.
  */
trait CirceImplicits extends AutoDerivation {

  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames.withSnakeCaseConstructorNames

  implicit val offsetDateTimeEncoder: Encoder[OffsetDateTime] = encodeOffsetDateTime
  implicit val offsetDateTimeDecoder: Decoder[OffsetDateTime] = decodeOffsetDateTime

  implicit val userIdEncoder: Encoder[UserId] = deriveUnwrappedEncoder
  implicit val userIdDecoder: Decoder[UserId] = deriveUnwrappedDecoder

  implicit val currencyEncoder: Encoder[Currency] = Encoder[String].contramap(_.getCurrencyCode)
  implicit val currencyDecoder: Decoder[Currency] = Decoder[String].emapTry(s => Try(Currency.getInstance(s)))

}
