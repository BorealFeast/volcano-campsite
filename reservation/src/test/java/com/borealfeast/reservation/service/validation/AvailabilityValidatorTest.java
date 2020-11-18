package com.borealfeast.reservation.service.validation;

import com.borealfeast.reservation.dao.AvailabilityPeriodEntity;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityValidatorTest {

    @Mock
    private CommonDateValidator commonDateValidator;

    @InjectMocks
    private AvailabilityValidator classUnderTest;

    @Test
    void validateAvailabilityNotAvailable() {
        AvailabilityPeriodEntity period = AvailabilityPeriodEntity.builder()
                .localStartDate(LocalDate.now())
                .build();
        List<AvailabilityPeriodEntity> currentBooking = new ArrayList<>(Arrays.asList(period));
        assertThrows(ResponseStatusException.class, () ->classUnderTest.validateAvailable(period, currentBooking));
    }

    @Test
    void validateAvailabilityIsAvailable() {
        AvailabilityPeriodEntity period = AvailabilityPeriodEntity.builder()
                .localStartDate(LocalDate.now())
                .build();
        List<AvailabilityPeriodEntity> currentBooking = new ArrayList<>(Arrays.asList(AvailabilityPeriodEntity.builder()
                .localStartDate(LocalDate.now().plusDays(3))
                .build()));
        assertDoesNotThrow(() ->classUnderTest.validateAvailable(period, currentBooking));
    }

    @Test
    void validateDateOrder() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        classUnderTest.validateDateOrder(startDate, endDate);
        Mockito.verify(commonDateValidator).validateDateOrder(startDate, endDate);
    }
}