package com.infy.service;

import com.infy.dto.VaccineAppointmentDTO;
import com.infy.entity.VaccineAppointment;
import com.infy.exception.InfyVaccineSchedulerException;
import com.infy.repository.VaccineAppointmentRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VaccineAppointmentServiceImpl implements VaccineAppointmentService {

    private final VaccineAppointmentRepository vaccineAppointmentRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public VaccineAppointmentServiceImpl(
            VaccineAppointmentRepository vaccineAppointmentRepository,
            ModelMapper modelMapper) {
        this.vaccineAppointmentRepository = vaccineAppointmentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public VaccineAppointmentDTO scheduleAppointment(VaccineAppointmentDTO vaccineAppointmentDTO)
            throws InfyVaccineSchedulerException {
        if (vaccineAppointmentDTO == null) {
            throw new InfyVaccineSchedulerException("Appointment details cannot be null");
        }
        VaccineAppointment appointment = modelMapper.map(vaccineAppointmentDTO, VaccineAppointment.class);
        appointment.setAppointmentId(null);
        if (appointment.getDoseNumber() == null) {
            appointment.setDoseNumber(1);
        }
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            appointment.setStatus("SCHEDULED");
        }
        VaccineAppointment saved = vaccineAppointmentRepository.save(appointment);
        return modelMapper.map(saved, VaccineAppointmentDTO.class);
    }

    @Override
    public List<VaccineAppointmentDTO> getAvailableAppointments(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String vaccineType) throws InfyVaccineSchedulerException {
        if (startDateTime.isAfter(endDateTime)) {
            throw new InfyVaccineSchedulerException(
                    "Start date/time must be before or equal to end date/time", HttpStatus.BAD_REQUEST);
        }
        List<VaccineAppointment> appointments =
                vaccineAppointmentRepository.findByAppointmentDateTimeBetweenAndVaccineTypeIgnoreCase(
                        startDateTime, endDateTime, vaccineType);
        return appointments.stream()
                .map(appointment -> modelMapper.map(appointment, VaccineAppointmentDTO.class))
                .toList();
    }
}
