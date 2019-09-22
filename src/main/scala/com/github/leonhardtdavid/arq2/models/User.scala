package com.github.leonhardtdavid.arq2.models

import com.github.leonhardtdavid.arq2.entities.UserId

/**
  * A user that belongs to a client and has access to this app.
  *
  * @param id        An optional id.
  * @param username  The user nick name.
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

/**
  * Class used to generate token for user.
  *
  * @param username The user nick name.
  * @param password The user password.
  */
case class UserToken(username: String, password: String)
