package com.infy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineCustomerDTO {

    private Integer customerId;

    @NotBlank(message = "{customer.firstName.absent}")
    private String firstName;

    @NotBlank(message = "{customer.lastName.absent}")
    private String lastName;

    @NotBlank(message = "{customer.email.absent}")
    @Email(message = "{customer.email.invalid}")
    private String email;

    @NotBlank(message = "{customer.phoneNumber.absent}")
    @Pattern(regexp = "^[0-9]{10}$", message = "{customer.phoneNumber.invalid}")
    private String phoneNumber;

    @NotNull(message = "{customer.dob.absent}")
    @Past(message = "{customer.dob.invalid}")
    private LocalDate dateOfBirth;

    private String status;
}
