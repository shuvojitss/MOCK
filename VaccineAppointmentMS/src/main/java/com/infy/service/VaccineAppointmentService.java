package com.infy.service;

import com.infy.dto.VaccineAppointmentDTO;
import com.infy.exception.InfyVaccineSchedulerException;

import java.time.LocalDateTime;
import java.util.List;

public interface VaccineAppointmentService {

    VaccineAppointmentDTO scheduleAppointment(VaccineAppointmentDTO vaccineAppointmentDTO)
            throws InfyVaccineSchedulerException;

    List<VaccineAppointmentDTO> getAvailableAppointments(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String vaccineType) throws InfyVaccineSchedulerException;
}
