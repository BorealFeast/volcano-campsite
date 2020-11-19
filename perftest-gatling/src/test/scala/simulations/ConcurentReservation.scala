package simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class ConcurentReservation extends Simulation {

  val tomorrow = LocalDate.now().plusDays(1);

  val rnd = new Random()

  // 1 Http Conf
  val httpConf = http.baseUrl("http://localhost:8080/api/v1/")
    .header("Accept", "application/json")
  //    .proxy(Proxy("localhost", 8888))

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomEmail() = {
    randomString(5) + "@" + randomString(5) + ".com"
  }

  val customFeeder = Iterator.continually(Map(
    "name" -> ("name-" + randomString(5)),
    "email" -> randomEmail()
  ))

  def getAvailabilities() = {
    exec(http("Get All Availabilities")
      .get("availabilities")
      .queryParam("startDate", getLocalDate(tomorrow))
      .check(jsonPath("$.periods[0].localStartDate").saveAs("localStartDate"))
      .check(jsonPath("$.periods[0].localEndDate").saveAs("availableEndDate")))
  }

  def generateReservationDates() = {
    exec { session =>
      val availableEndDate = getLocalDate(session("availableEndDate").as[String])
      val endDate = getLocalDate(session("localStartDate").as[String]).plusDays(Random.nextInt(3) + 1);
      if (endDate.isAfter(availableEndDate)) {
        session.set("localEndDate", getLocalDate(availableEndDate))
      } else {
        session.set("localEndDate", getLocalDate(endDate))
      }
    }
  }

  def createReservation() = {
    exec(
      tryMax(10) {
        exec(getAvailabilities())
          .exec(generateReservationDates())
          .exec(
            http("Create Reservation")
              .post("reservations")
              .body(ElFileBody("bodies/newReservation.json")).asJson
              .check(status.is(201))
              .check(jsonPath("$.id").saveAs("id")))

      })
      .exitHereIfFailed
  }

  def updateReservation(times: Int) = {

    repeat(times) {
      exec(getAvailabilities())
        .exec(generateReservationDates())
        .exec(
          http("Update Reservation")
            .put("reservations/${id}")
            .body(ElFileBody("bodies/updateReservation.json")).asJson
            .check(status.is(200)))
        .pause(1 seconds)
    }

  }

  def deleteReservation() = {
    exec(
      http("Delete Reservation")
        .delete("reservations/${id}"))
  }

  // 2 Scenario Definition
  val scnConcurentReservation = scenario("ConcurentReservation")
    .feed(customFeeder)
    .exec(createReservation())
    .pause(1 seconds)
    .exec(updateReservation(2))
    .pause(1 seconds)
    .exec(deleteReservation())

  // 2 Scenario Definition
  val scnGetAvailabilities = scenario("GetAvailabilities ")
    .exec(getAvailabilities())


  // 3 Load Scenario
  setUp(
    scnConcurentReservation.inject(
      atOnceUsers(3),
      constantUsersPerSec(3) during (5 seconds)
    ),
    scnGetAvailabilities.inject(
      rampUsersPerSec(600) to (800) during (15 seconds)
    )
  ).protocols(httpConf)

  def getLocalDate(localDate: LocalDate): String =
    DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)

  def getLocalDate(localDate: String): LocalDate = {
    LocalDate.parse(localDate, DateTimeFormatter.ISO_LOCAL_DATE)
  }

}
