package controllers.authentication

import io.circe.Json
import io.circe.syntax._
import play.api.Logger
import play.api.libs.circe.Circe
import play.api.mvc.{BaseController, _}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Abstract class with the authentication actions.
  *
  * @param logger  A logger instance.
  * @param handler A [[controllers.authentication.AuthenticationHandler]] instance.
  * @param ec      An implicit execution context.
  */
abstract class AuthenticationController(logger: Logger, handler: AuthenticationHandler)(implicit ec: ExecutionContext) {
  _: BaseController with Circe =>

  /**
    * A request wrapper with the user information.
    *
    * @param info    Authenticated user info (None if no identification).
    * @param request The request to be wrapped.
    * @tparam A Request type.
    */
  class AuthenticatedRequest[A](val info: Option[AuthInfo], request: Request[A]) extends WrappedRequest[A](request)

  /**
    * Obtain the [[controllers.authentication.AuthInfo]] from request and generates a
    * [[controllers.authentication.AuthenticationController.AuthenticatedRequest]] with it.
    */
  private class TokenAction[Body](val parser: BodyParser[Body])
      extends ActionBuilder[AuthenticatedRequest, Body]
      with ActionTransformer[Request, AuthenticatedRequest] {

    override protected def executionContext: ExecutionContext = ec

    override def transform[A](request: Request[A]): Future[AuthenticatedRequest[A]] =
      handler.principal(request) map (new AuthenticatedRequest(_, request))

  }

  /**
    * Verifies that the request has a valid user info.
    */
  private object AuthenticatedCheckAction extends ActionFilter[AuthenticatedRequest] {

    override protected def executionContext: ExecutionContext = ec

    override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = Future.successful {
      request.info match {
        case Some(_) => None
        case _       => Some(Unauthorized(Json.obj("error" -> "Unauthorized".asJson)))
      }
    }

  }

  /**
    * Before using this class you need to call first
    * [[controllers.authentication.AuthenticationController.AuthenticatedCheckAction]] to ensure a token exists,
    * otherwise this filter will fail.
    *
    * @param role role to check on token
    */
  private class RoleCheckAction(role: String) extends ActionFilter[AuthenticatedRequest] {

    override protected def executionContext: ExecutionContext = ec

    override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] =
      // TODO When the time comes, implement this method to validate that the user has the role needed.
      Future.successful(None)

  }

  /**
    * Action to verify the request has a valid token.
    *
    * @return A secured action.
    */
  def authAction(): ActionBuilder[AuthenticatedRequest, AnyContent] =
    new TokenAction(parse.anyContent) andThen AuthenticatedCheckAction

  /**
    * Action to verify the request has a valid token.
    *
    * @param parser A Body parser.
    * @tparam Body The body type.
    * @return A secured action.
    */
  def authAction[Body](parser: BodyParser[Body]): ActionBuilder[AuthenticatedRequest, Body] =
    new TokenAction(parser) andThen AuthenticatedCheckAction

  /**
    * Action to verify the request has a valid token and it has the required role.
    *
    * @param role Name of the role to verify.
    * @return A secured action that validates the given role.
    */
  def authActionWithRole(role: String): ActionBuilder[AuthenticatedRequest, AnyContent] =
    authAction() andThen new RoleCheckAction(role)

  /**
    * Action to verify the request has a valid token and it has the required role.
    *
    * @param role   Name of the role to verify.
    * @param parser A body parser.
    * @tparam Body The body type.
    * @return A secured action that validates the given role.
    */
  def authActionWithRole[Body](role: String, parser: BodyParser[Body]): ActionBuilder[AuthenticatedRequest, Body] =
    authAction(parser) andThen new RoleCheckAction(role)

}
