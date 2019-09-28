package com.github.leonhardtdavid.arq2.models

import com.github.leonhardtdavid.arq2.entities.{AssistanceId, Bringing, EventId, UserId}

/**
  * Class used to represent a database row for assistances.
  *
  * @param id       An optional id.
  * @param user     The user id.
  * @param event    The event id.
  * @param bringing What is the user bringing to the event.
  */
case class Assistance(
    id: Option[AssistanceId],
    user: Option[UserId],
    event: Option[EventId],
    bringing: Option[Bringing])
