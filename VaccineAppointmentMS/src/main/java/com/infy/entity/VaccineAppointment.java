package com.infy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vaccine_appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appointmentId;

    @Column(nullable = false)
    private Integer customerId;

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(nullable = false)
    private String vaccineType;

    private String vaccinationCenter;

    private Integer doseNumber;

    private String status;
}
