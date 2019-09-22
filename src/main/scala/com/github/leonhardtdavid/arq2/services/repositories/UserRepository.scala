package com.github.leonhardtdavid.arq2.services.repositories

import com.github.leonhardtdavid.arq2.entities.{User, UserId}
import javax.inject.Inject
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Client access to database.
  *
  * @param database A database access class.
  * @param ec       An implicit custom execution context.
  */
class UserRepository @Inject()(val database: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
    extends Repository[UserId, User] {

  import database.profile.api._

  implicit override protected def idColumn: BaseColumnType[UserId] = this.userIdColumn

  override protected type EntityTableType = Users
  val table: TableQuery[EntityTableType] = TableQuery[Users]

  /**
    * Query the database by the id to
    *
    * @param username The user nick name.
    * @return An asynchronous execution.
    */
  def findByUsername(username: String): DBIO[Option[User]] =
    this.table.filter(_.username === username).result.headOption

  // scalastyle:off
  class Users(tag: Tag) extends EntityTable(tag, "user") {

    def username  = column[String]("username", O.Unique)
    def password  = column[String]("password")
    def firstName = column[String]("first_name")
    def lastName  = column[String]("last_name")
    def email     = column[String]("email")

    override def * = (id ?, username, password, firstName, lastName, email) <> (User.tupled, User.unapply)

  }
  // scalastyle:on

}
