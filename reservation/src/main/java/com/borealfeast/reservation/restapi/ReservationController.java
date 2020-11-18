package com.borealfeast.reservation.restapi;

import com.borealfeast.reservation.restapi.dto.CreateReservation;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.borealfeast.reservation.restapi.validation.EmailValidator;
import com.borealfeast.reservation.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController()
@RequestMapping("/api/v1")
public class ReservationController {

    private final EmailValidator emailValidator;

    private final ReservationService reservationService;

    public ReservationController(EmailValidator emailValidator, ReservationService reservationService) {
        this.emailValidator = emailValidator;
        this.reservationService = reservationService;
    }

    @PostMapping("reservations")
    public ResponseEntity<Reservation> createReservation(@Validated @RequestBody CreateReservation createReservation) {
        emailValidator.validate(createReservation.getEmail());

        Reservation reservation = reservationService.createReservation(createReservation);
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + reservation.getId())).body(reservation);
    }

    @PutMapping("reservations/{reservationId}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable String reservationId, @Validated @RequestBody Reservation reservation) {
        emailValidator.validate(reservation.getEmail());

        Reservation updatedReservation = reservationService.updateReservation(reservationId, reservation);

        return ResponseEntity.ok(updatedReservation);
    }

    @GetMapping("reservations/{reservationId}")
    public ResponseEntity<Reservation> getReservation(@PathVariable String reservationId) {
        return ResponseEntity.ok(reservationService.getReservation(reservationId));
    }

    @DeleteMapping("reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable String reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }


}
