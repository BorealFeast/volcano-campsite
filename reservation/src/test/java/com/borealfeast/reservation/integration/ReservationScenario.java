package com.borealfeast.reservation.integration;

import com.borealfeast.reservation.restapi.dto.CreateReservation;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

@Component
public final class ReservationScenario {

    private static final String ENDPOINT = "/api/v1/reservations";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    public ReservationScenario(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public MvcResult createReservation(CreateReservation createReservation) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders.post(ENDPOINT)
                    .content(objectMapper.writeValueAsString(createReservation)))
                .andReturn();
    }

    public MvcResult getReservation(String reservationId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/{reservationId}", reservationId))
                .andReturn();
    }

    public MvcResult updateReservation(Reservation reservation) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders.put(ENDPOINT + "/{reservationId}", reservation.getId())
                        .content(objectMapper.writeValueAsString(reservation)))
                .andReturn();
    }

    public MvcResult deleteReservation(String reservationId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/{reservationId}", reservationId))
                .andReturn();
    }

}
