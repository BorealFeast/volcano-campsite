package com.borealfeast.reservation.dao;

import com.borealfeast.reservation.restapi.dto.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ReservationDaoFacade extends CrudRepository<ReservationEntity, String> {

}
