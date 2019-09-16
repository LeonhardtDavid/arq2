package controllers.authentication

import play.api.mvc.Request

import scala.concurrent.Future

/**
  * Authentication handler interface.
  */
trait AuthenticationHandler {

  /**
    * Creates a [[controllers.authentication.AuthInfo Principal]] instance from the request.
    *
    * @param request The request to get the authentication information.
    * @tparam A Request type.
    * @return A [[scala.Some]] if the authentication is valid, [[scala.None]] otherwise.
    */
  def principal[A](request: Request[A]): Future[Option[AuthInfo]]

}
