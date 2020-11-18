package com.borealfeast.reservation.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BookingByShortPeriodEntity {

    public BookingByShortPeriodEntity(int yearPeriod6) {
        this.yearPeriod6 = yearPeriod6;
        this.days = new HashSet<>();
    }

    public BookingByShortPeriodEntity(int yearPeriod6, Set<Integer> days) {
        this.yearPeriod6 = yearPeriod6;
        this.days = days;
    }

    @Id
    private int yearPeriod6;

    @Convert( converter = StringSetConverter.class)
    private Set<Integer> days;

}
