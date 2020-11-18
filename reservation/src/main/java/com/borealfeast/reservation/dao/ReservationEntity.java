package com.borealfeast.reservation.dao;

import lombok.*;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservationEntity {

    @Id
    private String id;

    private String name;

    private String email;

    private LocalDate localStartDate;

    private LocalDate localEndDate;

}
