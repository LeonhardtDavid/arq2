package com.github.leonhardtdavid.arq2.entities

import java.time.OffsetDateTime

/**
  * Class used to represent a database row for events.
  *
  * @param id          An optional id.
  * @param name        Event name.
  * @param image       An image for the event.
  * @param description An optional description.
  * @param capacity    How many people can assist.
  * @param date        Event date.
  * @param owner       Event's owner.
  * @param tag         An event's tag.
  * @param public      true if the event is public.
  * @param venue       Where the event is placed.
  */
case class Event(
    id: Option[EventId],
    name: String,
    image: String,
    description: Option[String],
    capacity: Int,
    date: OffsetDateTime,
    owner: UserId,
    tag: String,
    public: Boolean,
    venue: Venue)
    extends Entity[EventId]

/**
  * Where the event is placed.
  *
  * @param street  Street with name an height.
  * @param city    The city.
  * @param country The country.
  */
case class Venue(street: String, city: String, country: String)

/**
  * Event id wrapper.
  *
  * @param underlying Underlying id.
  */
case class EventId(underlying: Long) extends AnyVal
