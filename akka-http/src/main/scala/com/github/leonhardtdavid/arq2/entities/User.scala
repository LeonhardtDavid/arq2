package com.github.leonhardtdavid.arq2.entities

/**
  * Class used to represent a database row for users.
  *
  * @param id        An optional id.
  * @param username  The user name.
  * @param password  The user password.
  * @param firstName The user name.
  * @param lastName  The user last name.
  * @param email     The user email.
  */
case class User(
    id: Option[UserId],
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    email: String)
    extends Entity[UserId]

/**
  * User id wrapper.
  *
  * @param underlying Underlying id.
  */
case class UserId(underlying: Long) extends AnyVal
