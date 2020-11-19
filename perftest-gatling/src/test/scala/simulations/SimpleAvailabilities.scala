package simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class SimpleAvailabilities extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl("http://localhost:8080/api/v1/")
    .header("Accept", "application/json")
//    .proxy(Proxy("localhost", 8888))


  // 2 Scenario Definition
  val scn = scenario("SimpleAvailabilities")
    .exec(http("Get All Availabilities")
      .get("availabilities")
      .queryParam("startDate", getLocalDate(LocalDate.now())))


  // 3 Load Scenario
  setUp(
    scn.inject(
      atOnceUsers(10),
      rampUsersPerSec(10) to (50) during (10 seconds),
      constantUsersPerSec(75) during (10 seconds),

    )
  ).protocols(httpConf)

  def getLocalDate(localDate: LocalDate): String =
    DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)

}
