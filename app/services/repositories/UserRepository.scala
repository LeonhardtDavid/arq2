package services.repositories

import entities.{User, UserId}
import javax.inject.{Inject, Named, Singleton}
import models.configurations._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

/**
  * User repository.
  *
  * @param dbConfigProvider Database access provider.
  * @param ec               A custom execution context.
  */
@Singleton
class UserRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
)(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext)
    extends Repository[UserId, User] {

  import profile.api._

  implicit override protected def idColumn: BaseColumnType[UserId] = this.userIdColumn

  override protected type EntityTableType = Users
  val table: TableQuery[EntityTableType] = TableQuery[Users]

  // scalastyle:off
  class Users(tag: Tag) extends EntityTable(tag, "user") {

    def name = column[String]("name")

    override def * = (id ?, name) <> (User.tupled, User.unapply)

  }
  // scalastyle:on

}
