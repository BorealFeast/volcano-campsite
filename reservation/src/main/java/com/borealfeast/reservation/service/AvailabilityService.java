package com.borealfeast.reservation.service;

import com.borealfeast.reservation.dao.AvailabilityPeriodEntity;
import com.borealfeast.reservation.dao.BookingByLongPeriodDaoFacade;
import com.borealfeast.reservation.dao.BookingByShortPeriodDaoFacade;
import com.borealfeast.reservation.restapi.dto.AvailabilityPeriod;
import com.borealfeast.reservation.restapi.dto.AvailabilityPeriods;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.borealfeast.reservation.service.validation.AvailabilityValidator;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AvailabilityService {

    private final BookingByShortPeriodDaoFacade bookingByShortPeriodDaoFacade;

    private final BookingByLongPeriodDaoFacade bookingByLongPeriodDaoFacade;

    private final AvailabilityValidator availabilityValidator;

    public AvailabilityService(BookingByShortPeriodDaoFacade bookingByShortPeriodDaoFacade, BookingByLongPeriodDaoFacade bookingByLongPeriodDaoFacade, AvailabilityValidator availabilityValidator) {
        this.bookingByShortPeriodDaoFacade = bookingByShortPeriodDaoFacade;
        this.bookingByLongPeriodDaoFacade = bookingByLongPeriodDaoFacade;
        this.availabilityValidator = availabilityValidator;
    }


    public void reserve(Reservation reservation) {
        reserve(reservation, 2);
    }

    private void reserve(Reservation reservation, int retry) {
        AvailabilityPeriodEntity availabilityPeriod = getAvailabilityPeriod(reservation);
        List<AvailabilityPeriodEntity> currentBooking = bookingByShortPeriodDaoFacade.getPeriods(availabilityPeriod);
        availabilityValidator.validateAvailable(availabilityPeriod, currentBooking);
        try {
            bookingByShortPeriodDaoFacade.reserve(availabilityPeriod);
            bookingByLongPeriodDaoFacade.reserve(availabilityPeriod);
        } catch (DataIntegrityViolationException w) {
            assertRetry(retry);
            System.out.println("Retrying reserve");
            reserve(reservation, retry - 1);
        } catch (CannotAcquireLockException w) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict: Requested period is not available.");
        }
    }

    public void update(Reservation oldReservation, Reservation newReservation) {
        update(oldReservation, newReservation, 2);
    }

    private void update(Reservation oldReservation, Reservation newReservation, int retry) {
        assertRetry(retry);
        AvailabilityPeriodEntity newAvailabilityPeriod = getAvailabilityPeriod(newReservation);
        AvailabilityPeriodEntity oldAvailabilityPeriod = getAvailabilityPeriod(oldReservation);
        List<AvailabilityPeriodEntity> currentBooking = bookingByShortPeriodDaoFacade.getPeriods(newAvailabilityPeriod);
        availabilityValidator.validateAvailable(newAvailabilityPeriod, currentBooking);

        try {
            bookingByShortPeriodDaoFacade.update(oldAvailabilityPeriod, newAvailabilityPeriod);
            bookingByLongPeriodDaoFacade.free(oldAvailabilityPeriod);
            bookingByLongPeriodDaoFacade.reserve(newAvailabilityPeriod);
        } catch (DataIntegrityViolationException w) {
            System.out.println("Retrying Update");
            update(oldReservation, newReservation, retry - 1);
        } catch (CannotAcquireLockException w) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict: Requested period is not available.");
        }
    }

    public void free(Reservation reservation) {
        free(reservation, 2);
    }

    public void free(Reservation reservation, int retry) {
        assertRetry(retry);
        AvailabilityPeriodEntity availabilityPeriod = getAvailabilityPeriod(reservation);
        try {
            bookingByShortPeriodDaoFacade.free(availabilityPeriod);
            bookingByLongPeriodDaoFacade.free(availabilityPeriod);
        } catch (DataIntegrityViolationException w) {
            System.out.println("Retrying Free");
            free(reservation, retry - 1);
        } catch (CannotAcquireLockException w) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict: Requested period is not available.");
        }
    }

    private AvailabilityPeriodEntity getAvailabilityPeriod(Reservation reservation) {
        return AvailabilityPeriodEntity.builder()
                .id(reservation.getId())
                .localStartDate(reservation.getLocalStartDate())
                .localEndDate(reservation.getLocalEndDate())
                .build();
    }

    public AvailabilityPeriods getAvailabilities(LocalDate startDate, LocalDate endDate) {
        availabilityValidator.validateDateOrder(startDate, endDate);
        List<AvailabilityPeriodEntity> bookedPeriods = new ArrayList<>(bookingByLongPeriodDaoFacade.getPeriods(startDate, endDate));
        bookedPeriods.sort(Comparator.comparing(AvailabilityPeriodEntity::getLocalStartDate));
        List<AvailabilityPeriod> periods = new ArrayList<>();
        LocalDate curr = startDate;
        for (int i = 0; i < bookedPeriods.size(); i++) {
            LocalDate periodStartDate = bookedPeriods.get(i).getLocalStartDate();
            if (curr.isBefore(periodStartDate)) {
                periods.add(new AvailabilityPeriod(curr, earliest(endDate, periodStartDate)));
            }
            curr = bookedPeriods.get(i).getLocalEndDate();
        }
        if (curr.isBefore(endDate)) {
            periods.add(new AvailabilityPeriod(curr, endDate));
        }
        return new AvailabilityPeriods(periods);
    }

    private LocalDate earliest(LocalDate endDate, LocalDate periodStartDate) {
        return endDate.isBefore(periodStartDate) ? endDate : periodStartDate;
    }

    private void assertRetry(int retry) {
        if (retry <= 0) {
            throw new IllegalStateException("retry exhausted");
        }
    }

}
