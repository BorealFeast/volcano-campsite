package com.borealfeast.reservation.service.converter;

import com.borealfeast.reservation.dao.ReservationEntity;
import com.borealfeast.reservation.restapi.dto.Reservation;
import org.springframework.stereotype.Service;

@Service
public class ReservationConverter {

    public Reservation toReservation(ReservationEntity reservationEntity){
        return Reservation.builder()
                .id(reservationEntity.getId())
                .name(reservationEntity.getName())
                .email(reservationEntity.getEmail())
                .localEndDate(reservationEntity.getLocalEndDate())
                .localStartDate(reservationEntity.getLocalStartDate())
                .build();
    }

    public ReservationEntity toReservationEntity(Reservation reservation){
        return ReservationEntity.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .email(reservation.getEmail())
                .localEndDate(reservation.getLocalEndDate())
                .localStartDate(reservation.getLocalStartDate())
                .build();
    }

}
