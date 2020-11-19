# Volcano Campsite Booking Project

This is a project used to simulate a booking system for one campsite location throught a REST api that supports concurent requests.

#### reservation

The **reservation** maven module is a spring-boot projects that implements the REST api of the booking system. It also uses **posgresql** as its data store. 

The api documention can be found at the following location: `reservation/src/main/resources/api/v1/reservation-api.yml`


#### perftest-gatling

**perftest-gatling** is a maven module leverages Gatling framework to perform some performance tests. 


## Requirements
- JDK 11
- Docker

## Built with
- Maven


## Getting Started
To build the project, simply run at the root

```bash
./mvnw clean install
```

## Running the application

Run the following command to start and configure the database for the first time:
```bash
./setup.sh
```
You can later start and shutdown the database by running at the root:
```bash
docker-compose up
docker-compose down
```

Once the database has been started, you can now start the application using:
```bash
./mvnw -pl reservation spring-boot:run
```

You can look for available periods with the following curl, passing your prefered time frame:
```bash
curl --location --request GET 'localhost:8080/api/v1/availabilities?startDate=2020-11-19&endDate=2021-12-19'
```

You can create a reservation using the following curl:
```bash
curl --location --request POST 'localhost:8080/api/v1/reservations' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "John Doe",
    "email": "test@test.com",
    "localStartDate": "2020-12-18",
    "localEndDate": "2020-12-19"
}'
```

## Running the performance test

This run simulates 50 users that are racing for the first available periods, which then try to update their reservation and then cancel them.
Meanwhile, there is 600 to 800 users per second that are querying the availabiliies endpoint, all defined like so: 

```scala
class ConcurentReservation {
/** **/
	
  setUp(
    scnConcurentReservation.inject(
      atOnceUsers(5),
      constantUsersPerSec(3) during (15 seconds)
    ),
    scnGetAvailabilities.inject(
      rampUsersPerSec(600) to (800) during (15 seconds)
    )
  ).protocols(httpConf)
	
/** **/
}
```

To start the simulation, simply run:

```bash
./mvnw -pl perftest-gatling gatling:test -Dgatling.simulationClass=simulations.ConcurentReservation
```

