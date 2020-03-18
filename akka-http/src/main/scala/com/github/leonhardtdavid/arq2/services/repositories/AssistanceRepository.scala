package com.github.leonhardtdavid.arq2.services.repositories

import com.github.leonhardtdavid.arq2.entities._
import com.github.leonhardtdavid.arq2.models.config.DATABASE_DISPATCHER
import javax.inject.{Inject, Named}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Assistance access to database.
  *
  * @param database A database access class.
  * @param ec       An implicit custom execution context.
  */
class AssistanceRepository @Inject()(
    val database: DatabaseConfig[JdbcProfile]
  )(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext)
    extends Repository[AssistanceId, Assistance] {

  import database.profile.api._

  implicit override protected def idColumn: BaseColumnType[AssistanceId] = this.assistanceIdColumn

  override protected type EntityTableType = Assistances
  val table: TableQuery[EntityTableType] = TableQuery[Assistances]

  /**
    * Query the database by the user id.
    *
    * @param id The id of the user to get from the database.
    * @return An asynchronous execution.
    */
  def findByUserId(id: UserId): DBIO[List[Assistance]] = this.table.filter(_.user === id).result.map(_.toList)

  /**
    * Query the database by the event id.
    *
    * @param id The id of the event to get from the database.
    * @return An asynchronous execution.
    */
  def findByEventId(id: EventId): DBIO[List[Assistance]] = this.table.filter(_.event === id).result.map(_.toList)

  /**
    * Delete the assistances for the given event.
    *
    * @param eventId The event's id to remove.
    * @return true if delete was successful, otherwise false.
    */
  def deleteForEvent(eventId: EventId): DBIO[Boolean] = this.table.filter(_.event === eventId).delete.map(_ => true)

  /**
    * Count row quantity filtered by the event id.
    *
    * @param id The id of the event to get from the database.
    * @return An asynchronous execution.
    */
  def countByEventId(id: EventId): DBIO[Int] = this.table.filter(_.event === id).length.result

  /**
    * Count row quantity filtered by the user id.
    *
    * @param id The id of the event to get from the database.
    * @return An asynchronous execution.
    */
  def countByUserId(id: UserId): DBIO[Int] = this.table.filter(_.user === id).length.result

  // scalastyle:off
  class Assistances(tag: Tag) extends EntityTable(tag, "assistance") {

    def user  = column[UserId]("user_id")
    def event = column[EventId]("event_id")

    def bringingName     = column[String]("bringing_name")
    def bringingQuantity = column[Int]("bringing_quantity")
    def bringing         = (bringingName.?, bringingQuantity.?) <> (Bringing.tupled, Bringing.unapply)

    override def * = (id ?, user, event, bringing) <> (Assistance.tupled, Assistance.unapply)

  }
  // scalastyle:on

}
