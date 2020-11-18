package com.borealfeast.reservation.service;

import com.borealfeast.reservation.dao.ReservationDaoFacade;
import com.borealfeast.reservation.restapi.dto.CreateReservation;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.borealfeast.reservation.service.converter.ReservationConverter;
import com.borealfeast.reservation.service.validation.ReservationDataValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationDataValidator reservationValidator;

    private final ReservationDaoFacade reservationDao;

    private final AvailabilityService availabilityService;

    private final ReservationConverter reservationConverter;

    public ReservationService(ReservationDataValidator reservationValidator, ReservationDaoFacade reservationDaoFacade, AvailabilityService availabilityService, ReservationConverter reservationConverter) {
        this.reservationValidator = reservationValidator;
        this.reservationDao = reservationDaoFacade;
        this.availabilityService = availabilityService;
        this.reservationConverter = reservationConverter;
    }

    public Reservation createReservation(CreateReservation createReservation) {
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID().toString())
                .email(createReservation.getEmail())
                .name(createReservation.getName())
                .localStartDate(createReservation.getLocalStartDate())
                .localEndDate(createReservation.getLocalEndDate())
                .build();
        reservationValidator.validateCreate(reservation);
        availabilityService.reserve(reservation);

        reservationDao.save(reservationConverter.toReservationEntity(reservation));
        return reservation;
    }

    public Reservation updateReservation(String reservationId, Reservation newReservation) {
        Reservation oldReservation = getReservation(reservationId);
        if (Objects.equals(oldReservation, newReservation)) {
            return newReservation;
        }
        reservationValidator.validateUpdate(oldReservation, newReservation);
        //TODO merge free reserve
        availabilityService.free(oldReservation);
        availabilityService.reserve(newReservation);
        reservationDao.save(reservationConverter.toReservationEntity(newReservation));
        return newReservation;
    }

    public Reservation getReservation(String reservationId) {
        return reservationDao.findById(reservationId)
                .map(reservationConverter::toReservation)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ReservationId " + reservationId + " cannot be found."));
    }

    public void deleteReservation(String reservationId) {
        Reservation oldReservation = getReservation(reservationId);
        availabilityService.free(oldReservation);
        reservationDao.deleteById(reservationId);
    }

}
