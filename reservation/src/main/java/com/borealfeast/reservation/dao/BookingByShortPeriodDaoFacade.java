package com.borealfeast.reservation.dao;

import com.borealfeast.reservation.restapi.dto.Reservation;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;

@Repository
public class BookingByShortPeriodDaoFacade {

    public static final int PERIOD_LENGTH = 1;

    private final BookingByShortPeriodDao bookingByShortPeriodDao;

    public BookingByShortPeriodDaoFacade(BookingByShortPeriodDao bookingByShortPeriodDao) {
        this.bookingByShortPeriodDao = bookingByShortPeriodDao;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void update(AvailabilityPeriodEntity oldReservation, AvailabilityPeriodEntity newReservation) {
        free(oldReservation);
        reserve(newReservation);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void reserve(AvailabilityPeriodEntity period) {
        Set<Integer> yearPeriod6s = getInvolvedYearPeriods(period);
        Map<Integer, BookingByShortPeriodEntity> bookingEntities = getBookingAsMap(yearPeriod6s);

        for (Integer yearPeriod : yearPeriod6s) {
            bookingEntities.putIfAbsent(yearPeriod, new BookingByShortPeriodEntity(yearPeriod));
        }

        updateBooking(period, bookingEntities, this::markDayAsReserved);
        bookingByShortPeriodDao.saveAll(bookingEntities.values());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void free(AvailabilityPeriodEntity period) {
        Set<Integer> yearPeriod6s = getInvolvedYearPeriods(period);
        Map<Integer, BookingByShortPeriodEntity> bookingEntities = getBookingAsMap(yearPeriod6s);

        updateBooking(period, bookingEntities, this::markDayAsFree);

        Set<BookingByShortPeriodEntity> toUpdate = new HashSet<>();
        Set<BookingByShortPeriodEntity> toDelete = new HashSet<>();
        for (BookingByShortPeriodEntity booking : bookingEntities.values()) {
            if (booking.getDays().isEmpty()) {
                toDelete.add(booking);
            } else {
                toUpdate.add(booking);
            }
        }
        if (!toUpdate.isEmpty()) {
            bookingByShortPeriodDao.saveAll(toUpdate);
        }
        if (!toDelete.isEmpty()) {
            bookingByShortPeriodDao.deleteAll(toDelete);
        }
    }

    public List<AvailabilityPeriodEntity> getPeriods(AvailabilityPeriodEntity period) {
        Set<Integer> yearPeriod6s = new HashSet<>();
        yearPeriod6s.add(toYearPeriod(period.getLocalStartDate()));
        yearPeriod6s.add(toYearPeriod(period.getLocalEndDate()));

        List<AvailabilityPeriodEntity> availabilityPeriods = new ArrayList<>();
        for (BookingByShortPeriodEntity booking : bookingByShortPeriodDao.findAllById(yearPeriod6s)) {
            LocalDate base = toLocalDate(booking.getYearPeriod6());
            Set<Integer> days = booking.getDays();
            for (int i = 0; i < PERIOD_LENGTH; i++) {
                if (days.contains(i)) {
                    LocalDate date = base.plusDays(i);
                    availabilityPeriods.add(AvailabilityPeriodEntity.builder()
                            .localStartDate(date)
                            .localEndDate(date)
                            .build());
                }
            }
        }

        return availabilityPeriods;
    }

    private Map<Integer, BookingByShortPeriodEntity> getBookingAsMap(Set<Integer> yearPeriod6s) {
        Map<Integer, BookingByShortPeriodEntity> bookingEntities = new HashMap<>();
        for (BookingByShortPeriodEntity booking : bookingByShortPeriodDao.findAllById(yearPeriod6s)) {
            bookingEntities.put(booking.getYearPeriod6(), booking);
        }
        return bookingEntities;
    }

    private Set<Integer> getInvolvedYearPeriods(AvailabilityPeriodEntity period) {
        Set<Integer> yearPeriod6s = new HashSet<>();
        int startDateYearPeriod = toYearPeriod(period.getLocalStartDate());
        int endDateYearPeriod = toYearPeriod(period.getLocalEndDate().minusDays(1));
        yearPeriod6s.add(startDateYearPeriod);
        yearPeriod6s.add(endDateYearPeriod);
        return yearPeriod6s;
    }

    private void updateBooking(AvailabilityPeriodEntity period, Map<Integer, BookingByShortPeriodEntity> bookingEntities, BiConsumer<Set<Integer>, Integer> dayUpdateFunction) {
        LocalDate endDayInclusive = period.getLocalEndDate().minusDays(1);
        int startDateYearPeriod = toYearPeriod(period.getLocalStartDate());
        int endDateYearPeriod = toYearPeriod(endDayInclusive);
        for (BookingByShortPeriodEntity booking : bookingEntities.values()) {
            int yearPeriod = booking.getYearPeriod6();
            int startDay = 0;
            int endDay = PERIOD_LENGTH - 1;
            if (startDateYearPeriod >= yearPeriod) {
                startDay = period.getLocalStartDate().getDayOfYear() % PERIOD_LENGTH;
            }
            if (endDateYearPeriod <= yearPeriod) {
                endDay = endDayInclusive.getDayOfYear() % PERIOD_LENGTH;
            }
            Set<Integer> bookedDays = booking.getDays();
            for (int i = startDay; i <= endDay; i++) {
                dayUpdateFunction.accept(bookedDays, i);
            }
            booking.setDays(bookedDays);
        }
    }

    private void markDayAsFree(Set<Integer> bookedDays, int day) {
        bookedDays.remove(day);
    }

    private void markDayAsReserved(Set<Integer> bookedDays, int day) {
        bookedDays.add(day);
    }


    /**
     * Get the year and period number of a local date.
     * Ex:
     * if PERIOD_LENGTH == 6
     * 2020-01-01 would return 202000 => 2020 + 1/6
     * 2020-04-14 where may 14th is the 135th day in a leap year would return 202022 => 2020 + 135/6
     *
     * @param localDate
     * @return
     */
    private int toYearPeriod(LocalDate localDate) {
        return localDate.getYear() * 1000
                //Separates the year in buckets of PERIOD_LENGTH days long booking periods
                + localDate.getDayOfYear() / PERIOD_LENGTH;
    }

    private LocalDate toLocalDate(int yearPeriod6) {
        int year = yearPeriod6 / 1000;
        return LocalDate.ofYearDay(year, (yearPeriod6 - (year * 1000)) * PERIOD_LENGTH);
    }

}