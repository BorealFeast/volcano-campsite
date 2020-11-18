package com.borealfeast.reservation.service;

import com.borealfeast.reservation.dao.AvailabilityPeriodEntity;
import com.borealfeast.reservation.dao.BookingByLongPeriodDaoFacade;
import com.borealfeast.reservation.dao.BookingByShortPeriodDaoFacade;
import com.borealfeast.reservation.restapi.dto.AvailabilityPeriod;
import com.borealfeast.reservation.restapi.dto.AvailabilityPeriods;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.borealfeast.reservation.service.validation.AvailabilityValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
        AvailabilityPeriodEntity availabilityPeriod = getAvailabilityPeriod(reservation);
        List<AvailabilityPeriodEntity> currentBooking = bookingByShortPeriodDaoFacade.getPeriods(availabilityPeriod);
        availabilityValidator.validateAvailable(availabilityPeriod, currentBooking);
        bookingByShortPeriodDaoFacade.reserve(availabilityPeriod);
        bookingByLongPeriodDaoFacade.reserve(availabilityPeriod);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void free(Reservation reservation) {
        AvailabilityPeriodEntity availabilityPeriod = getAvailabilityPeriod(reservation);
        bookingByShortPeriodDaoFacade.free(availabilityPeriod);
        bookingByLongPeriodDaoFacade.free(availabilityPeriod);
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
            if(curr.isBefore(periodStartDate)){
                periods.add(new AvailabilityPeriod(curr, earliest(endDate, periodStartDate)));
            }
            curr = bookedPeriods.get(0).getLocalEndDate();
        }
        if(curr.isBefore(endDate)){
            periods.add(new AvailabilityPeriod(curr, endDate));
        }
        return new AvailabilityPeriods(periods);
    }

    private LocalDate earliest(LocalDate endDate, LocalDate periodStartDate) {
        return endDate.isBefore(periodStartDate) ? endDate :periodStartDate;
    }
}
