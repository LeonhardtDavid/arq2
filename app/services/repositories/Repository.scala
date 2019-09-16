package services.repositories

import java.sql.Timestamp
import java.time.{OffsetDateTime, ZoneOffset}
import java.util.Currency

import entities._
import javax.inject.Named
import models.configurations._
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Base implementation for database access.
  *
  * @param ec An implicit custom execution context.
  * @tparam ID         Type of the id used in the entity to persist.
  * @tparam EntityType The current type of the entity to persist.
  */
abstract class Repository[ID, EntityType <: Entity[ID]](implicit @Named(DATABASE_DISPATCHER) ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  protected val dbConfigProvider: DatabaseConfigProvider

  /**
    * An implicit type mapper for the entity id.
    *
    * @return A column mapper.
    */
  implicit protected def idColumn: BaseColumnType[ID]

  /**
    * Table to implement.
    */
  protected type EntityTableType <: EntityTable
  val table: TableQuery[EntityTableType]

  protected val logger = Logger(this.getClass)

  /**
    * Save or update an entity.
    *
    * @param data the entity to persist.
    * @return An asynchronous execution.
    */
  def save(data: EntityType): DBIO[ID] = data.id match {
    case Some(_: Long) => this.table.update(data).map(_ => data.id.get)
    case Some(id)      => this.table.insertOrUpdate(data).map(_ => id)
    case _             => this.table.returning(this.table.map(_.id)).into((_, id) => id) += data
  }

  /**
    * List the entities from the database.
    *
    * @return An asynchronous execution.
    */
  def list: DBIO[Seq[EntityType]] = this.table.result

  /**
    * Query the database by the id to
    *
    * @param id The id of the entity to get from the database.
    * @return An asynchronous execution.
    */
  def get(id: ID): DBIO[Option[EntityType]] = this.table.filter(_.id === id).result.headOption

  /**
    * Delete an entity
    *
    * @param id the entity's id to remove
    * @return true if delete was successful, otherwise false
    */
  def delete(id: ID): DBIO[Boolean] = this.table.filter(_.id === id).delete.map(_ != 0)

  /**
    * Count row quantity
    *
    * @return count result
    */
  def count: DBIO[Int] = this.table.length.result

  // scalastyle:off scaladoc
  implicit protected def userIdColumn: BaseColumnType[UserId] =
    MappedColumnType.base[UserId, Long](
      _.underlying,
      UserId.apply
    )

  implicit protected def timestamp2offsetDateTime: BaseColumnType[OffsetDateTime] =
    MappedColumnType.base[OffsetDateTime, Timestamp](
      dateTime => Timestamp.from(dateTime.withOffsetSameInstant(ZoneOffset.UTC).toInstant),
      date => OffsetDateTime.ofInstant(date.toInstant, ZoneOffset.UTC)
    )

  implicit protected def currencyColumn: BaseColumnType[Currency] =
    MappedColumnType.base[Currency, String](
      _.getCurrencyCode,
      Currency.getInstance
    )
  // scalastyle:on scaladoc

  /**
    * Abstract Table.
    *
    * @param tag        Something that is needed by slick.
    * @param tableName  Table name (lowercase for postgres).
    * @param schemaName Schema name (None for default).
    */
  abstract class EntityTable(tag: Tag, tableName: String, schemaName: Option[String] = None)
      extends Table[EntityType](tag, schemaName, tableName) {

    /**
      * Id representation for the database.
      *
      * @return A representation for this field.
      */
    def id: Rep[ID] = column[ID]("id", O.PrimaryKey, O.AutoInc)

  }

}
