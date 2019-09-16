package services.resources

import entities.UserId
import javax.inject.{Inject, Named}
import models._
import models.configurations._
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import services.repositories.UserRepository
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * User handler for database access
  *
  * @param dbConfigProvider Database provider.
  * @param logger           A [[play.api.Logger]] instance.
  * @param repository       A [[services.repositories.UserRepository]] instance.
  * @param ec               A custom execution context.
  */
class UserResourceHandler @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    logger: Logger,
    repository: UserRepository
  )(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  private def db2model(e: entities.User): User = User(e.id, e.name)

  /**
    * Save a [[models.User]]
    *
    * @param data the User to save or update
    * @return a [[scala.concurrent.Future]] with the result
    */
  def save(data: User): Future[User] = db.run {
    val dbUser = entities.User(data.id, data.name)

    this.repository.save(dbUser) map (id => data.copy(id = Some(id)))
  }

  /**
    * List [[models.User]]
    *
    * @return a [[scala.concurrent.Future]] with the result
    */
  def list: Future[List[User]] = db.run {
    logger.debug("Listing User")
    this.repository.list map (_ map db2model toList)
  }

  /**
    * Find a [[models.User]] by id
    *
    * @param id User's id to find
    * @return a [[scala.concurrent.Future]] with the result
    */
  def get(id: UserId): Future[Option[User]] = db.run {
    logger.debug(s"Getting User $id")
    this.repository.get(id) map (_ map db2model)
  }

  /**
    * Delete a [[models.User]] by id
    *
    * @param id rule's id to delete
    * @return a [[scala.concurrent.Future]] with the result
    */
  def delete(id: UserId): Future[Boolean] = db.run {
    this.repository.delete(id)
  }

  /**
    * Count rules quantity
    *
    * @return a [[scala.concurrent.Future]] with the result
    */
  def count: Future[Int] = db.run {
    logger.debug("Counting User")
    this.repository.count
  }

}
