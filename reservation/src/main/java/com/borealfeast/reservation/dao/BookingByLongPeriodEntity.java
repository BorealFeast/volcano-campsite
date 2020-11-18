package com.borealfeast.reservation.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bookings")
public class BookingByLongPeriodEntity {

    public BookingByLongPeriodEntity(int yearMonth) {
        this.yearMonth = yearMonth;
        this.availabilities = new HashSet<>();
    }

    public BookingByLongPeriodEntity(int yearMonth, Set<AvailabilityPeriodEntity> availabilities) {
        this.yearMonth = yearMonth;
        this.availabilities = availabilities;
    }

    @Id
    private int yearMonth;

    @ManyToMany(mappedBy = "bookings", fetch = FetchType.LAZY)
    private Set<AvailabilityPeriodEntity> availabilities;

}
