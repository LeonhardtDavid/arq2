package db

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

/**
  * Trait to have custom database connection
  */
trait PlayDBSpec extends PlaySpec with GuiceOneAppPerSuite {

  /**
    * Builder to have an application with in memory database
    *
    * @param db database name
    * @return a [[play.api.inject.guice.GuiceApplicationBuilder]] builder
    */
  protected def builder(db: String): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .in(Mode.Test)
      .configure(
        "slick.dbs.default.profile"   -> "slick.jdbc.H2Profile$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url"    -> s"jdbc:h2:mem:$db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE"
      )

}
