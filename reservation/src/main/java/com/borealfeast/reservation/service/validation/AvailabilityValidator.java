package com.borealfeast.reservation.service.validation;

import com.borealfeast.reservation.dao.AvailabilityPeriodEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvailabilityValidator {

    private final CommonDateValidator commonDateValidator;

    public AvailabilityValidator(CommonDateValidator commonDateValidator) {
        this.commonDateValidator = commonDateValidator;
    }

    public void validateAvailable(AvailabilityPeriodEntity period, List<AvailabilityPeriodEntity> currentBooking) {
        for (AvailabilityPeriodEntity booking : currentBooking) {
            if (booking.getLocalStartDate().equals(period.getLocalStartDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid: Requested period is not available.");
            }
        }
    }

    public void validateDateOrder(LocalDate startDate, LocalDate endDate) {
        commonDateValidator.validateDateOrder(startDate, endDate);
    }


}
