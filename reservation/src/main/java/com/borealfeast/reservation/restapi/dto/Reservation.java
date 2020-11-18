package com.borealfeast.reservation.restapi.dto;

import lombok.*;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private LocalDate localStartDate;

    @NonNull
    private LocalDate localEndDate;

}
