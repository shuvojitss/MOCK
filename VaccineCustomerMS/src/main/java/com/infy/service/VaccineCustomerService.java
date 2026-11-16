package com.infy.service;

import com.infy.dto.VaccineCustomerDTO;
import com.infy.exception.InfyVaccineSchedulerException;

import java.util.List;

public interface VaccineCustomerService {

    VaccineCustomerDTO registerCustomer(VaccineCustomerDTO customerDTO) throws InfyVaccineSchedulerException;

    VaccineCustomerDTO getCustomerById(Integer customerId) throws InfyVaccineSchedulerException;

    List<VaccineCustomerDTO> getAllCustomers() throws InfyVaccineSchedulerException;

    VaccineCustomerDTO updateCustomer(Integer customerId, VaccineCustomerDTO customerDTO)
            throws InfyVaccineSchedulerException;

    String deleteCustomer(Integer customerId) throws InfyVaccineSchedulerException;
}
