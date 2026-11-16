package com.infy.repository;

import com.infy.entity.VaccineCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaccineCustomerRepository extends JpaRepository<VaccineCustomer, Integer> {

    Optional<VaccineCustomer> findByEmailIgnoreCase(String email);
}
