import extractors._
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.HeaderValues
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class EventsSimulation extends Simulation {

  val httpConf: HttpProtocolBuilder = http
    .baseUrl("http://localhost:9000/api") // Here is the root for all relative URLs
    .acceptHeader("application/json;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val eventCreation: ScenarioBuilder = scenario("Create event").exec(
    TokenCreation.token,
    CreateEvent.createEvent
  )

  setUp(
    eventCreation.inject(
      atOnceUsers(1),
      rampUsersPerSec(1) to 10 during (20 seconds),
      constantUsersPerSec(10) during (20 seconds)
    )
  ).protocols(httpConf)

}

object TokenCreation {

  val tokenKey = "token"

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

object CreateEvent {

  val eventKey = "event"

  private val eventsFeeder = csv("gatling/conf/events.csv").random

  val createEvent: ChainBuilder =
    feed(eventsFeeder)
      .exec(
        http("Create Event")
          .post("/events")
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
              HttpHeaderNames.Authorization -> ("Bearer ${" + TokenCreation.tokenKey + "}")
            )
          )
          .check(
            status.is(201),
            headerRegex(HttpHeaderNames.Location, "^\\/api\\/events\\/(\\d*)$").ofType[Long].saveAs(eventKey)
          )
      )
      .pause(1 seconds)

}
