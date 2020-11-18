package com.borealfeast.reservation.integration;

import com.borealfeast.reservation.restapi.dto.CreateReservation;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

@Component
public final class AvailabilitiesScenario {

    private static final String ENDPOINT = "/api/v1/availabilities";

    private MockMvc mockMvc;

    public AvailabilitiesScenario(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }


    public MvcResult getAvailabilities(LocalDate startDate, LocalDate endDate) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "?startDate={startDate}&endDate={endDate}", startDate, endDate))
                .andReturn();
    }

    public MvcResult getAvailabilities() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT))
                .andReturn();
    }


}
