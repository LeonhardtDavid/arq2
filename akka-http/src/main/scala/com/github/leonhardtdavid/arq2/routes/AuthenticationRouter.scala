package com.github.leonhardtdavid.arq2.routes

import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directive}
import com.github.leonhardtdavid.arq2.services.JWTService

/**
  *
  */
trait AuthenticationRouter { _: Router =>

  val jwt: JWTService

  /**
    * Authenticated directive to extract token from authentication header.
    *
    * @return A directive to handle authentication.
    */
  protected def authenticated: Directive[Tuple1[String]] = {
    val authenticator: PartialFunction[Credentials, Option[String]] = {
      case Credentials.Provided(token) => this.jwt.decode(token)
      case _                           => None
    }

    authenticateOAuth2PF("", authenticator)
      .filter(_.isDefined, AuthenticationFailedRejection(CredentialsRejected, HttpChallenge("", None)))
      .map(_.get)
  }

}
