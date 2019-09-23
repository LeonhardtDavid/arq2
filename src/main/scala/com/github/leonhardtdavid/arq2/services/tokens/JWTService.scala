package com.github.leonhardtdavid.arq2.services.tokens

import java.time.Instant

import com.github.leonhardtdavid.arq2.models.config.JWTConfiguration
import io.circe.Json
import io.circe.syntax._
import javax.inject.Inject
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

/**
  * Json Web Tokens service for tokens generation and validation.
  *
  * @param config JWT configuration.
  */
class JWTService @Inject()(config: JWTConfiguration) {

  private val algorithm = JwtAlgorithm.HS512

  private val payloadKey = "username"

  /**
    * Generates a token for the given username.
    *
    * @param username The user nick name.
    * @return Signed JWT.
    */
  def encode(username: String): String = {
    val claim = JwtClaim(
      content = Json.obj(payloadKey -> username.asJson).noSpaces,
      expiration = Some(Instant.now().plusSeconds(this.config.duration.toSeconds).getEpochSecond),
      issuedAt = Some(Instant.now().getEpochSecond)
    )

    JwtCirce.encode(claim, this.config.key, this.algorithm)
  }

  /**
    * Decode the token if is valid.
    *
    * @param token A JWT token
    * @return True if the token is valid.
    */
  def decode(token: String): Option[String] =
    JwtCirce
      .decode(token, this.config.key, Seq(this.algorithm))
      .toOption
      .flatMap(claim => io.circe.parser.parse(claim.content).getOrElse(Json.obj()) \\ payloadKey headOption)
      .map(_.noSpaces)

}
