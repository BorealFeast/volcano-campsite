package com.borealfeast.reservation.restapi.validation;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailValidator {

    private static final String EMAIL_PATTERN = "^(.+)@(\\S+)$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public void validate(final String email) {
        Matcher matcher = pattern.matcher(email);
        if(!matcher.matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email format is not valid.");
        }
    }

}
