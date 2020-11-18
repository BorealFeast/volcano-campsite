package com.borealfeast.reservation.restapi;

import com.borealfeast.reservation.restapi.dto.AvailabilityPeriods;
import com.borealfeast.reservation.restapi.dto.Reservation;
import com.borealfeast.reservation.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController()
@RequestMapping("/api/v1")
public class AvailabilitiesController {

    private final static int DEFAULT_END_DATE_IN_DAYS = 30;

    private final AvailabilityService availabilityService;

    public AvailabilitiesController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }
    @GetMapping("availabilities")
    public ResponseEntity<AvailabilityPeriods> getReservation(
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate) {
        return ResponseEntity.ok(availabilityService.getAvailabilities(startDate, endDate.orElseGet(() -> startDate.plusDays(DEFAULT_END_DATE_IN_DAYS))));
    }


}
