package com.infy.repository;

import com.infy.entity.VaccineAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VaccineAppointmentRepository extends JpaRepository<VaccineAppointment, Integer> {

    List<VaccineAppointment> findByAppointmentDateTimeBetweenAndVaccineTypeIgnoreCase(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String vaccineType);
}
