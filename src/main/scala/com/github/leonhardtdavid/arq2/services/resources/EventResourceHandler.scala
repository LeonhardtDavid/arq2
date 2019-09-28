package com.github.leonhardtdavid.arq2.services.resources

import akka.event.LoggingAdapter
import com.github.leonhardtdavid.arq2.entities
import com.github.leonhardtdavid.arq2.entities.{Bringing, EventId, UserId}
import com.github.leonhardtdavid.arq2.models.config._
import com.github.leonhardtdavid.arq2.models.{Assistance, Event, Requirement}
import com.github.leonhardtdavid.arq2.services.repositories._
import javax.inject.{Inject, Named, Singleton}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Event handler for database access
  *
  * @param logger                 A database access instance.
  * @param database               Database provider.
  * @param repository             A [[com.github.leonhardtdavid.arq2.services.repositories.EventRepository]] instance.
  * @param requirementsRepository A [[com.github.leonhardtdavid.arq2.services.repositories.RequirementRepository]]
  *                               instance.
  * @param assistanceRepository   A [[com.github.leonhardtdavid.arq2.services.repositories.AssistanceRepository]]
  *                               instance.
  * @param ec                     A custom execution context.
  */
@Singleton
class EventResourceHandler @Inject()(
    logger: LoggingAdapter,
    database: DatabaseConfig[JdbcProfile],
    repository: EventRepository,
    requirementsRepository: RequirementRepository,
    assistanceRepository: AssistanceRepository
  )(implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext) {

  import database.profile.api._

  private val db = database.db

  private def db2model(rs: List[entities.Requirement])(e: entities.Event): Event = {
    val requirements = rs.map(r => Requirement(r.id, r.description, r.quantity))

    Event(e.id, e.name, e.image, e.description, e.capacity, e.date, e.owner, requirements, e.tag, e.public, e.venue)
  }

  private def model2db(data: Event): entities.Event =
    entities.Event(
      data.id,
      data.name,
      data.image,
      data.description,
      data.capacity,
      data.date,
      data.owner,
      data.tag,
      data.public,
      data.venue
    )

  private def bringing2model(bringing: Bringing) = bringing match {
    case Bringing(Some(_), Some(_)) => Some(bringing)
    case _                          => None
  }

  /**
    * Save a [[com.github.leonhardtdavid.arq2.models.Event]]
    *
    * @param data The Event to save or update.
    * @return a [[scala.concurrent.Future]] with the result.
    */
  def save(data: Event): Future[Event] = db.run {
    logger.info("Saving Event")

    val dbEvent = this.model2db(data)

    val query =
      for {
        eventId <- this.repository.save(dbEvent)
        requirementIds <- DBIOAction.sequence {
          val rs = data.requirements.map(r => entities.Requirement(r.id, eventId, r.description, r.quantity))
          rs.map(this.requirementsRepository.save)
        }
      } yield {
        val requirements = requirementIds.zip(data.requirements).map {
          case (id, requirement) => requirement.copy(id = Some(id))
        }

        data.copy(id = Some(eventId), requirements = requirements)
      }

    query.transactionally
  }

  /**
    * Save a [[com.github.leonhardtdavid.arq2.models.Event]]
    *
    * @param data The Event to save or update.
    * @return a [[scala.concurrent.Future]] with the result.
    */
  def update(data: Event): Future[Event] = db.run {
    logger.info("Updating Event")

    val dbEvent = this.model2db(data)

    val query =
      for {
        eventId <- this.repository.save(dbEvent)
        _       <- this.requirementsRepository.deleteForEvent(eventId)
        requirementIds <- DBIOAction.sequence {
          val rs = data.requirements.map(r => entities.Requirement(r.id, eventId, r.description, r.quantity))
          rs.map(this.requirementsRepository.save)
        }
      } yield {
        val requirements = requirementIds.zip(data.requirements).map {
          case (id, requirement) => requirement.copy(id = Some(id))
        }

        data.copy(id = Some(eventId), requirements = requirements)
      }

    query.transactionally
  }

  /**
    * List [[com.github.leonhardtdavid.arq2.models.Event]]
    *
    * @param from     From row.
    * @param quantity Result rows quantity.
    * @return a [[scala.concurrent.Future]] with the result
    */
  def list(from: Long, quantity: Int): Future[List[Event]] = db.run {
    logger.debug("Listing Event from {}, quantity {}", from, quantity)
    this.repository.list(from, quantity) map (_ map db2model(Nil) toList)
  }

  /**
    * Find a [[com.github.leonhardtdavid.arq2.models.Event]] by id
    *
    * @param id Event's id to find
    * @return a [[scala.concurrent.Future]] with the result
    */
  def findById(id: EventId): Future[Option[Event]] = db.run {
    logger.debug("Getting Event {}", id)

    for {
      requirements <- this.requirementsRepository.findByEventId(id)
      events       <- this.repository.findById(id) map (_ map db2model(requirements))
    } yield events
  }

  /**
    * Delete a [[com.github.leonhardtdavid.arq2.models.Event]] by id
    *
    * @param id Event's id to delete
    * @return a [[scala.concurrent.Future]] with the result
    */
  def delete(id: EventId): Future[Boolean] = db.run {
    logger.debug("Deleting Event {}", id)

    val query =
      for {
        requirementResult <- this.requirementsRepository.deleteForEvent(id)
        assistanceResult  <- this.assistanceRepository.deleteForEvent(id)
        eventResult       <- this.repository.delete(id)
      } yield requirementResult && assistanceResult && eventResult

    query.transactionally
  }

  /**
    * Count events quantity
    *
    * @return A [[scala.concurrent.Future]] with the result.
    */
  def count: Future[Int] = db.run {
    logger.debug("Counting Event")
    this.repository.count
  }

  /**
    * Try to create an assistance to an event for a user.
    *
    * @param userId     The user that wants to participate.
    * @param event      The Event.
    * @param assistance The assistance object submited.
    * @return A [[scala.concurrent.Future]] with the result.
    */
  def assist(userId: UserId, event: Event, assistance: Assistance): Future[Option[Assistance]] =
    db.run {
      val eventId = event.id.get

      for {
        count <- this.assistanceRepository.countByEventId(eventId) if count < event.capacity

        dbAssistance = entities.Assistance(assistance.id,
                                           userId,
                                           eventId,
                                           assistance.bringing.getOrElse(Bringing(None, None)))

        assistanceId <- this.assistanceRepository.save(dbAssistance)
      } yield Some(Assistance(Some(assistanceId), Some(userId), Some(eventId), assistance.bringing))
    } recover {
      case _: NoSuchElementException =>
        None
      case e: Throwable =>
        logger.error(e, "Error trying to create an assistance")
        throw e
    }

  /**
    * List all assistances for an event.
    *
    * @param eventId Event's id.
    * @return A [[scala.concurrent.Future]] with the result.
    */
  def listAssistances(eventId: EventId): Future[List[Assistance]] = db.run {
    this.assistanceRepository.findByEventId(eventId) map { list =>
      list map { dbAssistance =>
        Assistance(dbAssistance.id, Some(dbAssistance.user), Some(eventId), this.bringing2model(dbAssistance.bringing))
      }
    }
  }

  /**
    * List all assistances for a user.
    *
    * @param userId User's id.
    * @return A [[scala.concurrent.Future]] with the result.
    */
  def listAssistancesForUser(userId: UserId): Future[List[Assistance]] = db.run {
    this.assistanceRepository.findByUserId(userId) map { list =>
      list map { dbAssistance =>
        Assistance(dbAssistance.id, Some(userId), Some(dbAssistance.event), this.bringing2model(dbAssistance.bringing))
      }
    }
  }

  /**
    * Count assistances for an event.
    *
    * @param eventId The event's id.
    * @return A [[scala.concurrent.Future]] with the result.
    */
  def countAssistancesForEvent(eventId: EventId): Future[Int] = db.run {
    this.assistanceRepository.countByEventId(eventId)
  }

  /**
    * Count assistances for a user.
    *
    * @param userId The user's id.
    * @return A [[scala.concurrent.Future]] with the result.
    */
  def countAssistancesForUser(userId: UserId): Future[Int] = db.run {
    this.assistanceRepository.countByUserId(userId)
  }

}
