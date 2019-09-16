package models

import entities.UserId

/**
  * A user that belongs to a client and has access to this app.
  *
  * @param id           An optional id.
  * @param name         The user name.
  */
case class User(id: Option[UserId], name: String)
