package com.github.leonhardtdavid.arq2.routes

import cats.effect._
import com.github.leonhardtdavid.arq2.entities.UserId
import com.github.leonhardtdavid.arq2.models.User
import com.github.leonhardtdavid.arq2.models.json.CirceImplicits
import io.circe.syntax._
import org.http4s.MediaType._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._

/**
  * Users routes handler.
  */
class UsersRouter extends CirceImplicits {

  implicit val decoder: EntityDecoder[IO, User] = jsonOf[IO, User]

  /**
    * Creates the users route.
    *
    * @return A [[org.http4s.HttpRoutes]].
    */
  def usersRoutes: HttpRoutes[IO] = {
    val pathPrefix = "users"

    val dsl = new Http4sDsl[IO] {}

    import dsl._

    HttpRoutes.of[IO] {
      case req @ POST -> Root / `pathPrefix` =>
        for {
          _        <- req.as[User]
          response <- Created()
        } yield response.withHeaders(`Content-Type`(application.json), Location(Uri(path = s"${req.uri.path}/2")))

      case GET -> Root / `pathPrefix` / LongVar(id) =>
        Ok(User(Some(UserId(id)), "u", "p", "fn", "ln", "email").asJson)
    }
  }

}
