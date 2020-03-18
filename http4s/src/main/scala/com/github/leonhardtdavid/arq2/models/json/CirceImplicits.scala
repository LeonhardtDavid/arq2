package com.github.leonhardtdavid.arq2.models.json

import com.github.leonhardtdavid.arq2.entities.{AssistanceId, EventId, RequirementId, UserId}
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.{AutoDerivation, Configuration}
import io.circe.{Decoder, Encoder, Printer}

/**
  * Circe implicits.
  */
trait CirceImplicits extends AutoDerivation {

  implicit val customPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val userIdEncoder: Encoder[UserId] = deriveUnwrappedEncoder
  implicit val userIdDecoder: Decoder[UserId] = deriveUnwrappedDecoder

  implicit val eventIdEncoder: Encoder[EventId] = deriveUnwrappedEncoder
  implicit val eventIdDecoder: Decoder[EventId] = deriveUnwrappedDecoder

  implicit val requirementIdEncoder: Encoder[RequirementId] = deriveUnwrappedEncoder
  implicit val requirementIdDecoder: Decoder[RequirementId] = deriveUnwrappedDecoder

  implicit val assistanceIdEncoder: Encoder[AssistanceId] = deriveUnwrappedEncoder
  implicit val assistanceIdDecoder: Decoder[AssistanceId] = deriveUnwrappedDecoder

}
