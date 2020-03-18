package com.github.leonhardtdavid.arq2.entities

/**
  * Class used to represent a database row for requirements.
  *
  * @param id          An optional id.
  * @param event       Event id.
  * @param description A description of the need.
  * @param quantity    How many are required.
  */
case class Requirement(id: Option[RequirementId], event: EventId, description: String, quantity: Int)
    extends Entity[RequirementId]

/**
  * Requirement id wrapper.
  *
  * @param underlying Underlying id.
  */
case class RequirementId(underlying: Long) extends AnyVal
