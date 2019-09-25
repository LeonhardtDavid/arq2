package com.github.leonhardtdavid.arq2.services.repositories

import java.time.OffsetDateTime

import com.github.leonhardtdavid.arq2.entities.{Event, EventId, UserId, Venue}
import com.github.leonhardtdavid.arq2.models.config.DATABASE_DISPATCHER
import javax.inject.{Inject, Named}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Event access to database.
  *
  * @param database A database access class.
  * @param ec       An implicit custom execution context.
  */
class EventRepository @Inject()(
    val database: DatabaseConfig[JdbcProfile]
  )(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext)
    extends Repository[EventId, Event] {

  import database.profile.api._

  implicit override protected def idColumn: BaseColumnType[EventId] = this.eventIdColumn

  override protected type EntityTableType = Events
  val table: TableQuery[EntityTableType] = TableQuery[Events]

  // scalastyle:off
  class Events(tag: Tag) extends EntityTable(tag, "event") {

    def name        = column[String]("name")
    def image       = column[String]("image")
    def description = column[String]("description")
    def capacity    = column[Int]("capacity")
    def date        = column[OffsetDateTime]("date")(offsetDateTimeColumn)
    def owner       = column[UserId]("owner")
    def tagg        = column[String]("tag") // method with double g to avoid conflicts with slick tag class parameter
    def public      = column[Boolean]("public")

    def street  = column[String]("street")
    def city    = column[String]("city")
    def country = column[String]("country")
    def venue   = (street, city, country) <> (Venue.tupled, Venue.unapply)

    override def * =
      (id ?, name, image, description ?, capacity, date, owner, tagg, public, venue) <> (Event.tupled, Event.unapply)

  }
  // scalastyle:on

}
