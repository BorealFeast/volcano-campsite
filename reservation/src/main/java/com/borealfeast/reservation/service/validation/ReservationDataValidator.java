package com.borealfeast.reservation.service.validation;

import com.borealfeast.reservation.restapi.dto.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationDataValidator {

    private final int maximumReservationPeriodLengthInDays;

    private final int minimumDaysInAdvance;

    private final int maximumDaysInAdvance;

    private final ZoneId zoneId;

    private final CommonDateValidator commonDateValidator;

    public ReservationDataValidator(
            @Value("${maximum-reservation-period-length-in-bookedPeriods:3}")
                    int maximumReservationPeriodLengthInDays,
            @Value("${reservation-minimum-bookedPeriods-advance:1}")
                    int minimumDaysInAdvance,
            @Value("${reservation-maximum-bookedPeriods-advance:30}")
                    int maximumDaysInAdvance,
            @Value("${location-zoneId:UNDEFINED}")
                    String zoneIdValue, CommonDateValidator commonDateValidator) {
        this.maximumReservationPeriodLengthInDays = maximumReservationPeriodLengthInDays;
        this.minimumDaysInAdvance = minimumDaysInAdvance;
        this.maximumDaysInAdvance = maximumDaysInAdvance;
        this.commonDateValidator = commonDateValidator;
        if ("UNDEFINED".equals(zoneIdValue)) {
            this.zoneId = ZoneId.systemDefault();
        } else {
            this.zoneId = ZoneId.of(zoneIdValue);
        }
    }

    public void validateCreate(Reservation reservation) {
        commonDateValidator.validateDateOrder(reservation.getLocalStartDate(), reservation.getLocalEndDate());
        validatePeriodLength(reservation);
        validateStartDate(reservation.getLocalStartDate());
    }

    public void validateUpdate(Reservation oldReservation, Reservation newReservation) {
        if (areStaticFieldUpdated(oldReservation, newReservation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid: id, email and name cannot be updated.");
        }
        validateCreate(newReservation);
    }
    private boolean areStaticFieldUpdated(Reservation oldReservation, Reservation newReservation) {
        if (!Objects.equals(oldReservation.getId(), newReservation.getId())) {
            return true;
        }
        if (!Objects.equals(oldReservation.getName(), newReservation.getName())) {
            return true;
        }
        if (!Objects.equals(oldReservation.getEmail(), newReservation.getEmail())) {
            return true;
        }
        return false;
    }

    private void validateStartDate(LocalDate localStartDate) {
        LocalDate now = LocalDate.now(zoneId);
        long days = DAYS.between(now, localStartDate);
        if (days < minimumDaysInAdvance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid: Cannot reserve period before : " + now.plusDays(minimumDaysInAdvance));
        }
        if (days >= maximumDaysInAdvance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid: Cannot reserve period with more than " + maximumDaysInAdvance + " bookedPeriods in the future.");
        }
    }

    private void validatePeriodLength(Reservation reservation) {
        long days = DAYS.between(reservation.getLocalStartDate(), reservation.getLocalEndDate());
        if (days > maximumReservationPeriodLengthInDays) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid: Cannot reserve a period longer than " + maximumReservationPeriodLengthInDays + " days.");
        }
        if (days < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid: Cannot reserve a period smaller than 1 day.");
        }
    }

}
