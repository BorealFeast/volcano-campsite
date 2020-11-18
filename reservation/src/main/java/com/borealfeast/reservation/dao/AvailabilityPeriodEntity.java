package com.borealfeast.reservation.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="availabilities")
public class AvailabilityPeriodEntity {

    @Id
    private String id;

    private LocalDate localStartDate;

    private LocalDate localEndDate;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "availabilities_bookings",
            joinColumns = {
                    @JoinColumn(name = "availabilities_id", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "bookings_yearMonth", referencedColumnName = "yearMonth",
                            nullable = false, updatable = false)})
    @Builder.Default
    private Set<BookingByLongPeriodEntity> bookings = new HashSet<>();

}
