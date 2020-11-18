package com.borealfeast.reservation.service.validation;

import com.borealfeast.reservation.restapi.dto.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReservationDataValidatorTest {

    public static final String EMAIL = "EMAIl";
    public static final String ID = "RandomId";
    public static final String NAME = "myName";
    private final int maximumReservationPeriodLengthInDays = 3;

    private final int minimumDaysInAdvance = 1;

    private final int maximumDaysInAdvance = 30;

    @Mock
    private CommonDateValidator commonDateValidator;

    private ReservationDataValidator classUnderTest;

    @BeforeEach
    void before() {
        classUnderTest = new ReservationDataValidator(maximumReservationPeriodLengthInDays,
                minimumDaysInAdvance,
                maximumDaysInAdvance,
                "UNDEFINED",
                commonDateValidator);
    }

    @Test
    void validateCreatePeriodTooLong() {
        LocalDate startDate = LocalDate.now().plusDays(minimumDaysInAdvance);
        LocalDate endDate = startDate.plusDays(maximumReservationPeriodLengthInDays + 1);
        Reservation reservation = createReservation(startDate, endDate);
        assertThrows(ResponseStatusException.class, () -> classUnderTest.validateCreate(reservation));
    }

    @Test
    void validateCreateStartsTooSoon() {
        LocalDate startDate = LocalDate.now().plusDays(maximumDaysInAdvance);
        LocalDate endDate = startDate.plusDays(maximumReservationPeriodLengthInDays);
        Reservation reservation = createReservation(startDate, endDate);
        assertThrows(ResponseStatusException.class, () -> classUnderTest.validateCreate(reservation));
    }

    @Test
    void validateCreateStartsTooLate() {
        LocalDate startDate = LocalDate.now().plusDays(minimumDaysInAdvance - 1);
        LocalDate endDate = startDate.plusDays(maximumReservationPeriodLengthInDays);
        Reservation reservation = createReservation(startDate, endDate);
        assertThrows(ResponseStatusException.class, () -> classUnderTest.validateCreate(reservation));
    }

    @Test
    void validateCreateAllValid() {
        LocalDate startDate = LocalDate.now().plusDays(minimumDaysInAdvance);
        LocalDate endDate = startDate.plusDays(maximumReservationPeriodLengthInDays);
        Reservation reservation = createReservation(startDate, endDate);
        assertDoesNotThrow(() -> classUnderTest.validateCreate(reservation));
        Mockito.verify(commonDateValidator).validateDateOrder(startDate, endDate);
    }

    @Test
    void validateUpdateStaticFields() {
        LocalDate startDate = LocalDate.now().plusDays(minimumDaysInAdvance);
        LocalDate endDate = startDate.plusDays(maximumReservationPeriodLengthInDays);
        Reservation reservation = createReservation(startDate, endDate);
        Reservation reservationUpdated = createReservation(startDate, endDate);
        reservationUpdated.setId("NewId");
        assertThrows(ResponseStatusException.class, ()  -> classUnderTest.validateUpdate(reservation, reservationUpdated));
    }

    @Test
    void validateUpdateIsValid() {
        LocalDate startDate = LocalDate.now().plusDays(minimumDaysInAdvance);
        LocalDate endDate = startDate.plusDays(maximumReservationPeriodLengthInDays);
        Reservation reservation = createReservation(startDate, endDate);
        Reservation reservationUpdated = createReservation(startDate.plusDays(1), endDate.plusDays(1));
        assertDoesNotThrow(() -> classUnderTest.validateUpdate(reservation, reservationUpdated));
    }

    private Reservation createReservation(LocalDate startDate, LocalDate endDate) {
        return Reservation.builder()
                .email(EMAIL)
                .name(NAME)
                .id(ID)
                .localStartDate(startDate)
                .localEndDate(endDate)
                .build();
    }

    @Test
    void validateUpdate() {
    }
}