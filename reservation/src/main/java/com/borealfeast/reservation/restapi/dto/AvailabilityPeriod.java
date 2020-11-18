package com.borealfeast.reservation.restapi.dto;

import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AvailabilityPeriod {

    private LocalDate localStartDate;

    private LocalDate localEndDate;

}
