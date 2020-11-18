package com.borealfeast.reservation.restapi.validation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    void validateWhenEmailIsNotValid() {
        EmailValidator classUnderTest = new EmailValidator();
        assertThrows(ResponseStatusException.class, ()->classUnderTest.validate("notAnEmail"));
    }

    @Test
    void validateWhenEmailIsValid() {
        EmailValidator classUnderTest = new EmailValidator();
        assertDoesNotThrow(()->classUnderTest.validate("myfavorite@test.com"));
    }
}