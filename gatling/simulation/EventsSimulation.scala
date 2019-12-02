import extractors._
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.HeaderValues
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.util.Random

class EventsSimulation extends Simulation {

  val httpConf: HttpProtocolBuilder = http
    .baseUrl("http://localhost:9000/api") // Here is the root for all relative URLs
    .acceptHeader("application/json;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val eventCreation: ScenarioBuilder = scenario("Create event scenario").exec(
    TokenExecutions.token,
    EventExecutions.listEvents,
    EventExecutions.createEvent
  )

  val eventUpdate: ScenarioBuilder = scenario("Update event scenario").exec(
    TokenExecutions.token,
    EventExecutions.createEvent,
    EventExecutions.getEvent,
    EventExecutions.updateEvent
  )

  val eventsList: ScenarioBuilder = scenario("List events scenario").exec(
    TokenExecutions.token,
    EventExecutions.listEvents
  )

  val eventDelete: ScenarioBuilder = scenario("Delete event scenario").exec(
    TokenExecutions.token,
    EventExecutions.createEvent,
    EventExecutions.getEvent,
    EventExecutions.deleteEvent,
    EventExecutions.getEventThatDoesNotExist
  )

  setUp(
    eventCreation.inject(
      atOnceUsers(1),
      rampUsersPerSec(1) to 5 during (60 seconds),
      constantUsersPerSec(5) during (10 minutes)
    ),
    eventUpdate.inject(
      atOnceUsers(1),
      rampUsersPerSec(1) to 5 during (60 seconds),
      constantUsersPerSec(5) during (10 minutes)
    ),
    eventsList.inject(
      atOnceUsers(1),
      rampUsersPerSec(1) to 5 during (60 seconds),
      constantUsersPerSec(5) during (10 minutes)
    ),
    eventDelete.inject(
      atOnceUsers(1),
      rampUsersPerSec(1) to 5 during (60 seconds),
      constantUsersPerSec(5) during (10 minutes)
    )
  ).protocols(httpConf)

}

object TokenExecutions {

  val tokenKey    = "token"
  val tokenHeader = s"Bearer $${$tokenKey}"

  private val userFeeder = csv("gatling/conf/users.csv").random

  val token: ChainBuilder =
    feed(userFeeder)
      .exec(
        http("Create Token")
          .post("/users/tokens")
          .body(StringBody("""{"username": "${username}", "password": "${password}"}"""))
          .header(HttpHeaderNames.ContentType, HeaderValues.ApplicationJson)
          .check(
            status.is(200),
            jsonPath(s"$$.$tokenKey").saveAs(tokenKey)
          )
      )
      .pause(7 seconds)

}

object EventExecutions {

  val eventKey   = "event"
  val eventIdKey = "eventId"

  private val eventsFeeder = csv("gatling/conf/events.csv").random
  private val eventPath    = "/events"

  val createEvent: ChainBuilder =
    feed(eventsFeeder)
      .exec(
        http("Create Event")
          .post(eventPath)
          .body(
            StringBody(
              """
              |{
              |	 "name": "${name}",
              |	 "description": "${description}",
              |	 "image": "${image}",
              |	 "capacity": ${capacity},
              |	 "date": "${date}",
              |	 "owner": 9999,
              |	 "requirements": [
              |	 	 {
              |	 	 	 "description": "${reqDescription}",
              |	 	 	 "quantity": ${reqQuantity}
              |	 	 }
              |	 ],
              |	 "public": true,
              |	 "tag": "${tag}",
              |	 "venue": {
              |	 	 "street": "${street}",
              |	 	 "city": "${city}",
              |	 	 "country": "${country}"
              |	 }
              |}
              |""".stripMargin
            )
          )
          .headers(
            Map(
              HttpHeaderNames.ContentType   -> HeaderValues.ApplicationJson,
              HttpHeaderNames.Authorization -> TokenExecutions.tokenHeader
            )
          )
          .check(
            status.is(201),
            headerRegex(HttpHeaderNames.Location, "^\\/api\\/events\\/(\\d*)$").ofType[Long].saveAs(eventIdKey)
          )
      )
      .pause(1 second)

  val getEvent: ChainBuilder =
    exec(
      http("Get Event")
        .get(s"$eventPath/$${$eventIdKey}")
        .header(HttpHeaderNames.Authorization, TokenExecutions.tokenHeader)
        .check(
          status.is(200),
          jsonPath("$.id").is(s"$${$eventIdKey}"),
          jsonPath("$").saveAs(eventKey)
        )
    ).pause(1 second)

  val updateEvent: ChainBuilder =
    feed(eventsFeeder)
      .exec(
        http("Update Event")
          .put(s"$eventPath/$${$eventIdKey}")
          .body(
            StringBody(
              s"""
                |{
                |	 "name": "$${name}",
                |	 "description": "$${description}",
                |	 "image": "$${image}",
                |	 "capacity": $${capacity},
                |	 "date": "$${date}",
                |	 "owner": 9999,
                |	 "requirements": [],
                |	 "public": true,
                |	 "tag": "RandomTag${Random.nextInt(100)}",
                |	 "venue": {
                |	 	 "street": "$${street}",
                |	 	 "city": "$${city}",
                |	 	 "country": "$${country}"
                |	 }
                |}
                |""".stripMargin
            )
          )
          .headers(
            Map(
              HttpHeaderNames.ContentType   -> HeaderValues.ApplicationJson,
              HttpHeaderNames.Authorization -> TokenExecutions.tokenHeader
            )
          )
          .check(status.is(200))
      )
      .pause(1 second)

  val listEvents: ChainBuilder =
    exec(
      http("List Events")
        .get(eventPath)
        .header(HttpHeaderNames.Authorization, TokenExecutions.tokenHeader)
        .check(
          status.is(200),
          jsonPath("$.items").exists,
          jsonPath("$.count").exists
        )
    ).pause(1 second)

  val deleteEvent: ChainBuilder =
    exec(
      http("Delete Event")
        .delete(s"$eventPath/$${$eventIdKey}")
        .header(HttpHeaderNames.Authorization, TokenExecutions.tokenHeader)
        .check(status.is(204))
    ).pause(1 second)

  val getEventThatDoesNotExist: ChainBuilder =
    exec(
      http("Get Event that doesn't exist")
        .get(s"$eventPath/$${$eventIdKey}")
        .header(HttpHeaderNames.Authorization, TokenExecutions.tokenHeader)
        .check(status.is(404))
    ).pause(1 second)

}
