package com.github.leonhardtdavid.arq2.services.resources

import akka.event.LoggingAdapter
import com.github.leonhardtdavid.arq2.entities
import com.github.leonhardtdavid.arq2.entities.UserId
import com.github.leonhardtdavid.arq2.models.config._
import com.github.leonhardtdavid.arq2.models.{User, UserToken}
import com.github.leonhardtdavid.arq2.services.repositories.UserRepository
import javax.inject.{Inject, Named, Singleton}
import org.mindrot.jbcrypt.BCrypt
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * User handler for database access
  *
  * @param logger     A database access instance.
  * @param database   Database provider.
  * @param repository A [[com.github.leonhardtdavid.arq2.services.repositories.UserRepository]] instance.
  * @param ec         A custom execution context.
  */
@Singleton
class UserResourceHandler @Inject()(
    logger: LoggingAdapter,
    database: DatabaseConfig[JdbcProfile],
    repository: UserRepository
  )(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext) {

  private val db = database.db

  private def db2model(e: entities.User): User = User(e.id, e.username, e.password, e.firstName, e.lastName, e.email)

  /**
    * Save a [[com.github.leonhardtdavid.arq2.models.User]]
    *
    * @param data the User to save or update
    * @return a [[scala.concurrent.Future]] with the result
    */
  def save(data: User): Future[User] = db.run {
    val salt           = BCrypt.gensalt()
    val hashedPassword = BCrypt.hashpw(data.password, salt)

    val dbUser = entities.User(data.id, data.username, hashedPassword, data.firstName, data.lastName, data.email)

    this.repository.save(dbUser) map (id => data.copy(id = Some(id)))
  }

  /**
    * List [[com.github.leonhardtdavid.arq2.models.User]]
    *
    * @return a [[scala.concurrent.Future]] with the result
    */
  def list: Future[List[User]] = db.run {
    logger.debug("Listing User")
    this.repository.list map (_ map db2model toList)
  }

  /**
    * Find a [[com.github.leonhardtdavid.arq2.models.User]] by id
    *
    * @param id User's id to find
    * @return a [[scala.concurrent.Future]] with the result
    */
  def findById(id: UserId): Future[Option[User]] = db.run {
    logger.debug("Getting User {}", id)
    this.repository.findById(id) map (_ map db2model)
  }

  /**
    * Find a [[com.github.leonhardtdavid.arq2.models.User]] by nick name.
    *
    * @param username The user nick name.
    * @return a [[scala.concurrent.Future]] with the result
    */
  def findByUsername(username: String): Future[Option[User]] = db.run {
    logger.debug("Getting User by username {}", username)
    this.repository.findByUsername(username) map (_ map db2model)
  }

  /**
    * Delete a [[com.github.leonhardtdavid.arq2.models.User]] by id
    *
    * @param id User's id to delete
    * @return a [[scala.concurrent.Future]] with the result
    */
  def delete(id: UserId): Future[Boolean] = db.run {
    logger.debug("Deleting User {}", id)
    this.repository.delete(id)
  }

  /**
    * Count users quantity
    *
    * @return a [[scala.concurrent.Future]] with the result
    */
  def count: Future[Int] = db.run {
    logger.debug("Counting User")
    this.repository.count
  }

  /**
    * Validate if the password (plaintext) is valid for the user (hashed password).
    *
    * @param user The user nick name and password.
    * @return True if password is valid, false otherwise.
    */
  def validatePassword(user: UserToken): Future[Boolean] =
    this.findByUsername(user.username) map { maybeDBUser =>
      maybeDBUser.fold(false)(dbUser => BCrypt.checkpw(user.password, dbUser.password))
    }

}
