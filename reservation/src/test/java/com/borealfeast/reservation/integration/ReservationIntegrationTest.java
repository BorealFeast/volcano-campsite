package com.borealfeast.reservation.integration;


import com.borealfeast.reservation.restapi.dto.CreateReservation;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationIntegrationTest extends BaseIntegrationTest{

    private final static String NAME = "myName";
    private final static String EMAIL = "test@test.com";

    @Autowired
    private ReservationScenario scenario;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenCreateReservation_WhenAllAvailable_ThenReservationIsCreated() throws Exception {
        CreateReservation createReservation = CreateReservation.builder()
                .name(NAME)
                .email(EMAIL)
                .localStartDate(coupleDaysLater())
                .localEndDate(coupleDaysLater().plusDays(1)).build();
        MvcResult result = scenario.createReservation(createReservation);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Reservation reservation = getResponseBody(result, Reservation.class);
        Assert.assertEquals(createReservation.getEmail(), reservation.getEmail());
        Assert.assertEquals(createReservation.getName(), reservation.getName());
        Assert.assertEquals(createReservation.getLocalStartDate(), reservation.getLocalStartDate());
        Assert.assertEquals(createReservation.getLocalEndDate(), reservation.getLocalEndDate());
        Assert.assertNotNull(reservation.getId());

        result = scenario.getReservation(reservation.getId());
        Assert.assertEquals(200, result.getResponse().getStatus());
        Reservation getReservation = getResponseBody(result, Reservation.class);
        Assert.assertEquals(createReservation.getEmail(), getReservation.getEmail());
        Assert.assertEquals(createReservation.getName(), getReservation.getName());
        Assert.assertEquals(createReservation.getLocalStartDate(), getReservation.getLocalStartDate());
        Assert.assertEquals(createReservation.getLocalEndDate(), getReservation.getLocalEndDate());
        Assert.assertNotNull(getReservation.getId());
    }

    @Test
    public void givenCreatedReservation_WhenUpdateReservationTime_ThenReservationIsCreated() throws Exception {
        CreateReservation createReservation = CreateReservation.builder()
                .name(NAME)
                .email(EMAIL)
                .localStartDate(coupleDaysLater())
                .localEndDate(coupleDaysLater().plusDays(1)).build();
        MvcResult result = scenario.createReservation(createReservation);

        Reservation reservation = getResponseBody(result, Reservation.class);
        reservation.setLocalStartDate(reservation.getLocalStartDate().plusDays(5));
        reservation.setLocalEndDate(reservation.getLocalEndDate().plusDays(5));

        result = scenario.updateReservation(reservation);
        Assert.assertEquals(200, result.getResponse().getStatus());
        Reservation updatedReservation = getResponseBody(result, Reservation.class);

        Assert.assertEquals(reservation.getId(), updatedReservation.getId());
        Assert.assertEquals(reservation.getEmail(), updatedReservation.getEmail());
        Assert.assertEquals(reservation.getName(), updatedReservation.getName());
        Assert.assertEquals(reservation.getLocalStartDate(), updatedReservation.getLocalStartDate());
        Assert.assertEquals(reservation.getLocalEndDate(), updatedReservation.getLocalEndDate());
    }

    @Test
    public void givenCreatedReservation_WhenDeleteReservationTime_Then404() throws Exception {
        CreateReservation createReservation = CreateReservation.builder()
                .name(NAME)
                .email(EMAIL)
                .localStartDate(coupleDaysLater())
                .localEndDate(coupleDaysLater().plusDays(1)).build();
        MvcResult result = scenario.createReservation(createReservation);

        Reservation reservation = getResponseBody(result, Reservation.class);
        result = scenario.deleteReservation(reservation.getId());
        Assert.assertEquals(204, result.getResponse().getStatus());

        result = scenario.getReservation(reservation.getId());
        Assert.assertEquals(404, result.getResponse().getStatus());
    }

    private <T> T getResponseBody(MvcResult result, Class<T> clazz) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    private LocalDate coupleDaysLater() {
        return LocalDate.now().plusDays(3);
    }


}
