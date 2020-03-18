package com.github.leonhardtdavid.arq2.models

import java.time.OffsetDateTime

import com.github.leonhardtdavid.arq2.entities.{EventId, UserId, Venue}

/**
  * An event.
  *
  * @param id           An optional id.
  * @param name         Event name.
  * @param image        An image for the event.
  * @param description  An optional description.
  * @param capacity     How many people can assist.
  * @param date         Event date.
  * @param owner        Event's owner.
  * @param requirements Requirements to bring to the event.
  * @param tag          An event's tag.
  * @param public       true if the event is public.
  * @param venue        Where the event is placed.
  */
case class Event(
    id: Option[EventId],
    name: String,
    image: String,
    description: Option[String],
    capacity: Int,
    date: OffsetDateTime,
    owner: UserId,
    requirements: List[Requirement],
    tag: String,
    public: Boolean,
    venue: Venue)
