package com.borealfeast.reservation.restapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservation {

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private LocalDate localStartDate;

    @NonNull
    private LocalDate localEndDate;
}
