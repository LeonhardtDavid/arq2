package com.github.leonhardtdavid.arq2.entities

/**
  * Class used to represent a database row for assistances.
  *
  * @param id       An optional id.
  * @param user     The user id.
  * @param event    The event id.
  * @param bringing What is the user bringing to the event.
  */
case class Assistance(id: Option[AssistanceId], user: UserId, event: EventId, bringing: Bringing)
    extends Entity[AssistanceId]

/**
  * Assistance id wrapper.
  *
  * @param underlying Underlying id.
  */
case class AssistanceId(underlying: Long) extends AnyVal

/**
  * Item the user is bringing, most probably an event requirement.
  *
  * @param name     What is bringing.
  * @param quantity How many of it.
  */
case class Bringing(name: Option[String], quantity: Option[Int])
