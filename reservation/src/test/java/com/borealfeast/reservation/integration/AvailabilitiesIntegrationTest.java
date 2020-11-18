package com.borealfeast.reservation.integration;


import com.borealfeast.reservation.restapi.dto.AvailabilityPeriods;
import com.borealfeast.reservation.restapi.dto.CreateReservation;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;


public class AvailabilitiesIntegrationTest extends BaseIntegrationTest {

    private final static String NAME = "myName";
    private final static String EMAIL = "test@test.com";

    @Autowired
    private ReservationScenario scenario;

    @Autowired
    private AvailabilitiesScenario avaialbilitiesScenario;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenNoReservations_WhenGettingAvailabilities_ThenAllAvailable() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(50);
        MvcResult result = avaialbilitiesScenario.getAvailabilities(startDate, endDate);

        Assert.assertEquals(200, result.getResponse().getStatus());
        AvailabilityPeriods periods = getResponseBody(result, AvailabilityPeriods.class);
        Assert.assertEquals(1 , periods.getPeriods().size());
        Assert.assertEquals(startDate , periods.getPeriods().get(0).getLocalStartDate());
        Assert.assertEquals(endDate , periods.getPeriods().get(0).getLocalEndDate());

    }

    @Test
    public void givenNoReservations_WhenGettingAvailabilitiesWithNoEndDate_ThenAllAvailableUpTo30daysLater() throws Exception {
        LocalDate startDate = LocalDate.now();
        MvcResult result = avaialbilitiesScenario.getAvailabilities(startDate, null);

        Assert.assertEquals(200, result.getResponse().getStatus());
        AvailabilityPeriods periods = getResponseBody(result, AvailabilityPeriods.class);
        Assert.assertEquals(1 , periods.getPeriods().size());
        Assert.assertEquals(startDate , periods.getPeriods().get(0).getLocalStartDate());
        Assert.assertEquals(startDate.plusDays(30) , periods.getPeriods().get(0).getLocalEndDate());

    }

    @Test
    public void givenCreatedReservation_WhenGettingAvailabilities_ThenAvailabilitiesOutsideOfReservation() throws Exception {

        LocalDate reservationStartDate = coupleDaysLater();
        LocalDate reservationEndDate = reservationStartDate.plusDays(1);

        CreateReservation createReservation = CreateReservation.builder()
                .name(NAME)
                .email(EMAIL)
                .localStartDate(reservationStartDate)
                .localEndDate(reservationEndDate).build();
        scenario.createReservation(createReservation);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(50);
        MvcResult result = avaialbilitiesScenario.getAvailabilities(startDate, endDate);

        Assert.assertEquals(200, result.getResponse().getStatus());
        AvailabilityPeriods periods = getResponseBody(result, AvailabilityPeriods.class);
        Assert.assertEquals(2 , periods.getPeriods().size());
        Assert.assertEquals(startDate , periods.getPeriods().get(0).getLocalStartDate());
        Assert.assertEquals(reservationStartDate , periods.getPeriods().get(0).getLocalEndDate());
        Assert.assertEquals(reservationEndDate , periods.getPeriods().get(1).getLocalStartDate());
        Assert.assertEquals(endDate , periods.getPeriods().get(1).getLocalEndDate());
    }

    @Test
    public void givenCreatedReservation_WhenReservationGetsDeleted_ThenAvailabilitiesAllAvailable() throws Exception {

        LocalDate reservationStartDate = coupleDaysLater();
        LocalDate reservationEndDate = reservationStartDate.plusDays(1);

        CreateReservation createReservation = CreateReservation.builder()
                .name(NAME)
                .email(EMAIL)
                .localStartDate(reservationStartDate)
                .localEndDate(reservationEndDate).build();
        MvcResult result = scenario.createReservation(createReservation);
        Reservation reservation = getResponseBody(result, Reservation.class);

        scenario.deleteReservation(reservation.getId());

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(50);
        result = avaialbilitiesScenario.getAvailabilities(startDate, endDate);


        Assert.assertEquals(200, result.getResponse().getStatus());
        AvailabilityPeriods periods = getResponseBody(result, AvailabilityPeriods.class);
        Assert.assertEquals(1 , periods.getPeriods().size());
        Assert.assertEquals(startDate , periods.getPeriods().get(0).getLocalStartDate());
        Assert.assertEquals(endDate , periods.getPeriods().get(0).getLocalEndDate());
    }


    private <T> T getResponseBody(MvcResult result, Class<T> clazz) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    private LocalDate coupleDaysLater() {
        return LocalDate.now().plusDays(3);
    }


}
