package com.borealfeast.reservation.service.validation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonDateValidatorTest {

    @Test
    void validateEndDateIsAfter() {
        CommonDateValidator classUnderTest = new CommonDateValidator();
        assertThrows(ResponseStatusException.class, ()->classUnderTest.validateDateOrder(LocalDate.now(), LocalDate.MIN));
    }

    @Test
    void validateWhenEmailIsValid() {
        CommonDateValidator classUnderTest = new CommonDateValidator();
        assertDoesNotThrow(()->classUnderTest.validateDateOrder(LocalDate.now(), LocalDate.MAX));
    }
}