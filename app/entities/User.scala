package entities

/**
  * Class used to represent a database row for users.
  *
  * @param id           An optional id
  * @param name         The user name.
  */
case class User(id: Option[UserId], name: String) extends Entity[UserId]

/**
  * User id wrapper.
  *
  * @param underlying Underlying id.
  */
case class UserId(underlying: Long) extends AnyVal
