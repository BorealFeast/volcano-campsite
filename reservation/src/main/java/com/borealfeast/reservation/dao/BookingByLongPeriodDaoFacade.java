package com.borealfeast.reservation.dao;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

@Repository
public class BookingByLongPeriodDaoFacade {

    private final BookingByLongPeriodDao bookingByLongPeriodDao;

    private final AvailabilityPeriodEntityDao availabilityPeriodEntityDao;

    public BookingByLongPeriodDaoFacade(BookingByLongPeriodDao bookingByLongPeriodDao, AvailabilityPeriodEntityDao availabilityPeriodEntityDao) {
        this.bookingByLongPeriodDao = bookingByLongPeriodDao;
        this.availabilityPeriodEntityDao = availabilityPeriodEntityDao;
    }

    public void reserve(AvailabilityPeriodEntity period) {
        Set<Integer> yearMonths = getInvolvedYearPeriods(period);
        Map<Integer, BookingByLongPeriodEntity> bookingEntities = getBookingAsMap(yearMonths);

        List<BookingByLongPeriodEntity> toUpdate = new ArrayList<>();
        for (Integer yearPeriod : yearMonths) {
            BookingByLongPeriodEntity booking;
            if (!bookingEntities.containsKey(yearPeriod)) {
                booking = new BookingByLongPeriodEntity(yearPeriod);
                toUpdate.add(booking);
            } else {
                booking = bookingEntities.get(yearPeriod);
            }
            period.getBookings().add(booking);
        }

        if (!toUpdate.isEmpty())
            bookingByLongPeriodDao.saveAll(toUpdate);
        availabilityPeriodEntityDao.save(period);

    }

    public void free(AvailabilityPeriodEntity period) {
        Set<Integer> yearMonths = getInvolvedYearPeriods(period);
        List<BookingByLongPeriodEntity> toDelete = new ArrayList<>();
        List<BookingByLongPeriodEntity> toUpdate = new ArrayList<>();
        for (BookingByLongPeriodEntity booking : bookingByLongPeriodDao.findAllById(yearMonths)) {
            booking.getAvailabilities().remove(period.getId());
            if (booking.getAvailabilities().isEmpty()) {
                toDelete.add(booking);
            } else {
                toUpdate.add(booking);
            }
        }
        if (!toUpdate.isEmpty()) {
            bookingByLongPeriodDao.saveAll(toUpdate);
        }
        if (!toDelete.isEmpty()) {
            bookingByLongPeriodDao.deleteAll(toDelete);
        }
        availabilityPeriodEntityDao.delete(period);
    }

    public Collection<AvailabilityPeriodEntity> getPeriods(LocalDate startDate, LocalDate endDate) {
        Set<Integer> yearMonths = getInvolvedYearPeriods(startDate, endDate);

        Map<String, AvailabilityPeriodEntity> bookingEntities = new HashMap<>();
        for (BookingByLongPeriodEntity booking : bookingByLongPeriodDao.findAllById(yearMonths)) {
            bookingEntities.putAll(booking.getAvailabilities().stream().collect(Collectors.toMap(AvailabilityPeriodEntity::getId, Function.identity())));
        }
        return bookingEntities.values();
    }

    private Map<Integer, BookingByLongPeriodEntity> getBookingAsMap(Set<Integer> yearMonth) {
        Map<Integer, BookingByLongPeriodEntity> bookingEntities = new HashMap<>();
        for (BookingByLongPeriodEntity booking : bookingByLongPeriodDao.findAllById(yearMonth)) {
            bookingEntities.put(booking.getYearMonth(), booking);
        }
        return bookingEntities;
    }

    private Set<Integer> getInvolvedYearPeriods(AvailabilityPeriodEntity period) {
        return getInvolvedYearPeriods(period.getLocalStartDate(), period.getLocalEndDate());
    }

    private Set<Integer> getInvolvedYearPeriods(LocalDate startDate, LocalDate endDate) {
        int months = (int) MONTHS.between(startDate, endDate);
        Set<Integer> yearPeriod6s = new HashSet<>();
        for(int i = 0; i <= months; i++){
            yearPeriod6s.add(toYearMonth(startDate.plusMonths(i)));
        }
        return yearPeriod6s;
    }

    private int toYearMonth(LocalDate localDate) {
        return localDate.getYear() * 100
                + localDate.getMonthValue() % 12;
    }
}