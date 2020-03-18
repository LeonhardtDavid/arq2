package com.github.leonhardtdavid.arq2.models

import com.github.leonhardtdavid.arq2.entities.RequirementId

/**
  * A requirement for an event.
  *
  * @param id          An optional id.
  * @param description A description of the need.
  * @param quantity    How many are required.
  */
case class Requirement(id: Option[RequirementId], description: String, quantity: Int)
