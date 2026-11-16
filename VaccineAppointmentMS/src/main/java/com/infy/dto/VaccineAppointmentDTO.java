package com.infy.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineAppointmentDTO {

    private Integer appointmentId;

    @NotNull(message = "{appointment.customerId.absent}")
    @Positive(message = "{appointment.customerId.invalid}")
    private Integer customerId;

    @NotNull(message = "{appointment.dateTime.absent}")
    @FutureOrPresent(message = "{appointment.dateTime.future}")
    private LocalDateTime appointmentDateTime;

    @NotBlank(message = "{appointment.vaccineType.absent}")
    private String vaccineType;

    private String vaccinationCenter;

    @Positive(message = "{appointment.doseNumber.invalid}")
    private Integer doseNumber;

    private String status;

    private VaccineCustomerDTO vaccineCustomerDTO;
}
