package entities

/**
  * Trait used in classes to be saved in data base, to have a common id to be used in the repositories.
  *
  * @tparam ID type of the id
  */
trait Entity[ID] {

  /**
    * Method to be overridden with a val.
    *
    * @return The real id.
    */
  def id: Option[ID]

}
