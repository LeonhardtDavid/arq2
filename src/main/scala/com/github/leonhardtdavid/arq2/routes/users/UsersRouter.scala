package com.github.leonhardtdavid.arq2.routes.users

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.leonhardtdavid.arq2.models.{User, UserToken}
import com.github.leonhardtdavid.arq2.routes.json.CirceImplicits
import com.github.leonhardtdavid.arq2.routes.{AuthenticationRouter, Router}
import com.github.leonhardtdavid.arq2.services.JWTService
import com.github.leonhardtdavid.arq2.services.resources.UserResourceHandler
import io.circe.Json
import io.circe.syntax._
import javax.inject.Inject

import scala.concurrent.ExecutionContext

/**
  * Users router.
  *
  * @param handler A database handler instance.
  * @param jwt     JWT token creator and validator.
  * @param ec      An implicit [[scala.concurrent.ExecutionContext]] instance.
  */
class UsersRouter @Inject()(handler: UserResourceHandler, val jwt: JWTService)(implicit ec: ExecutionContext)
    extends Router
    with AuthenticationRouter
    with CirceImplicits {

  override val routes: Route =
    pathPrefix("users") {
      pathEnd {
        save
      } ~
        path("tokens") {
          createToken
        }
    }

  private def save =
    authenticated { _ =>
      (post & entity(as[User])) { req =>
        extractRequestContext { context =>
          complete {
            this.handler.save(req.copy(id = None)) map { user =>
              this.created(s"${context.request.uri.toRelative.toString()}/${user.id.get.underlying}")
            }
          }
        }
      }
    }

  private def createToken =
    (post & entity(as[UserToken])) { user =>
      complete {
        this.handler.validatePassword(user) map { valid =>
          if (valid) {
            this.ok(Json.obj("token" -> this.jwt.encode(user.username).asJson))
          } else {
            this.notFound("Not a valid user")
          }
        }
      }
    }

}
