package com.infy.api;

import com.infy.dto.VaccineAppointmentDTO;
import com.infy.dto.VaccineCustomerDTO;
import com.infy.exception.InfyVaccineSchedulerException;
import com.infy.service.VaccineAppointmentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointment")
@Validated
public class VaccineAppointmentAPI {

    private static final String CUSTOMER_SERVICE_URL = "http://VaccineCustomerMS/vaccine/customers/{customerId}";

    @Autowired
    private VaccineAppointmentService vaccineAppointmentService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostMapping("/schedule")
    public ResponseEntity<VaccineAppointmentDTO> scheduleAppointment(
            @Valid @RequestBody VaccineAppointmentDTO vaccineAppointmentDTO) throws InfyVaccineSchedulerException {
        VaccineCustomerDTO customerDTO;
        try {
            customerDTO = fetchCustomerById(vaccineAppointmentDTO.getCustomerId());
        } catch (WebClientResponseException.NotFound ex) {
            throw new InfyVaccineSchedulerException(
                    "Customer not found with ID: " + vaccineAppointmentDTO.getCustomerId(),
                    HttpStatus.NOT_FOUND);
        }

        VaccineAppointmentDTO scheduledAppointment =
                vaccineAppointmentService.scheduleAppointment(vaccineAppointmentDTO);
        scheduledAppointment.setVaccineCustomerDTO(customerDTO);
        return new ResponseEntity<>(scheduledAppointment, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    @CircuitBreaker(name = "vaccineAppointmentService", fallbackMethod = "getCustomerDetailsFallBack")
    public ResponseEntity<List<VaccineAppointmentDTO>> getAvailableAppointments(
            @RequestParam @NotNull(message = "{appointment.startDateTime.absent}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @NotNull(message = "{appointment.endDateTime.absent}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestParam @NotBlank(message = "{appointment.vaccineType.absent}") String vaccineType)
            throws InfyVaccineSchedulerException {

        List<VaccineAppointmentDTO> availableAppointments =
                vaccineAppointmentService.getAvailableAppointments(startDateTime, endDateTime, vaccineType);

        for (VaccineAppointmentDTO appointmentDTO : availableAppointments) {
            Integer customerId = appointmentDTO.getCustomerId();
            VaccineCustomerDTO customerDTO = fetchCustomerById(customerId);
            appointmentDTO.setVaccineCustomerDTO(customerDTO);
        }

        return new ResponseEntity<>(availableAppointments, HttpStatus.OK);
    }

    public ResponseEntity<List<VaccineAppointmentDTO>> getCustomerDetailsFallBack(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String vaccineType,
            Throwable throwable) throws InfyVaccineSchedulerException {
        if (throwable instanceof InfyVaccineSchedulerException schedulerException) {
            throw schedulerException;
        }
        if (throwable instanceof ConstraintViolationException violationException) {
            throw violationException;
        }
        VaccineCustomerDTO customerDTO = new VaccineCustomerDTO();
        customerDTO.setCustomerId(0);
        customerDTO.setFirstName("Service ");
        customerDTO.setLastName("Unavailable ");
        customerDTO.setEmail("service.unavailable@fallback.com");
        customerDTO.setPhoneNumber("0000000000");
        customerDTO.setDateOfBirth(LocalDate.of(2000, 1, 1));
        customerDTO.setStatus("FALLBACK");

        VaccineAppointmentDTO appointmentDTO = new VaccineAppointmentDTO();
        appointmentDTO.setVaccineCustomerDTO(customerDTO);

        return new ResponseEntity<>(List.of(appointmentDTO), HttpStatus.BAD_REQUEST);
    }

    private VaccineCustomerDTO fetchCustomerById(Integer customerId) {
        return webClientBuilder.build()
                .get()
                .uri(CUSTOMER_SERVICE_URL, customerId)
                .retrieve()
                .bodyToMono(VaccineCustomerDTO.class)
                .block();
    }
}
