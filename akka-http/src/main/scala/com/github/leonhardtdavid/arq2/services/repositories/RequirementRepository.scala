package com.github.leonhardtdavid.arq2.services.repositories

import com.github.leonhardtdavid.arq2.entities.{EventId, Requirement, RequirementId}
import com.github.leonhardtdavid.arq2.models.config.DATABASE_DISPATCHER
import javax.inject.{Inject, Named}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Requirement access to database.
  *
  * @param database A database access class.
  * @param ec       An implicit custom execution context.
  */
class RequirementRepository @Inject()(
    val database: DatabaseConfig[JdbcProfile]
  )(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext)
    extends Repository[RequirementId, Requirement] {

  import database.profile.api._

  implicit override protected def idColumn: BaseColumnType[RequirementId] = this.requirementIdColumn

  override protected type EntityTableType = Requirements
  val table: TableQuery[EntityTableType] = TableQuery[Requirements]

  /**
    * Query the database by the event id.
    *
    * @param id The id of the event to get from the database.
    * @return An asynchronous execution.
    */
  def findByEventId(id: EventId): DBIO[List[Requirement]] = this.table.filter(_.event === id).result.map(_.toList)

  /**
    * Delete the requirements for the given event.
    *
    * @param eventId The event's id to remove.
    * @return true if delete was successful, otherwise false.
    */
  def deleteForEvent(eventId: EventId): DBIO[Boolean] = this.table.filter(_.event === eventId).delete.map(_ => true)

  // scalastyle:off
  class Requirements(tag: Tag) extends EntityTable(tag, "requirement") {

    def event       = column[EventId]("event_id")
    def description = column[String]("description")
    def quantity    = column[Int]("quantity")

    override def * = (id ?, event, description, quantity) <> (Requirement.tupled, Requirement.unapply)

  }
  // scalastyle:on

}
