package com.github.leonhardtdavid.arq2.routes.json

import com.github.leonhardtdavid.arq2.entities.{EventId, RequirementId, UserId}
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.{AutoDerivation, Configuration}
import io.circe.{Decoder, Encoder}

/**
  * Circe implicits.
  */
trait CirceImplicits extends AutoDerivation {

  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames.withSnakeCaseConstructorNames

  implicit val userIdEncoder: Encoder[UserId] = deriveUnwrappedEncoder
  implicit val userIdDecoder: Decoder[UserId] = deriveUnwrappedDecoder

  implicit val eventIdEncoder: Encoder[EventId] = deriveUnwrappedEncoder
  implicit val eventIdDecoder: Decoder[EventId] = deriveUnwrappedDecoder

  implicit val requirementIdEncoder: Encoder[RequirementId] = deriveUnwrappedEncoder
  implicit val requirementIdDecoder: Decoder[RequirementId] = deriveUnwrappedDecoder

}
