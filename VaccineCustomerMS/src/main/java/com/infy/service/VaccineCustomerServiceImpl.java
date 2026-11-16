package com.infy.service;

import com.infy.dto.VaccineCustomerDTO;
import com.infy.entity.VaccineCustomer;
import com.infy.exception.InfyVaccineSchedulerException;
import com.infy.repository.VaccineCustomerRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class VaccineCustomerServiceImpl implements VaccineCustomerService {

    private final VaccineCustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public VaccineCustomerServiceImpl(VaccineCustomerRepository customerRepository, ModelMapper modelMapper) {
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public VaccineCustomerDTO registerCustomer(VaccineCustomerDTO customerDTO) throws InfyVaccineSchedulerException {
        if (customerRepository.findByEmailIgnoreCase(customerDTO.getEmail()).isPresent()) {
            throw new InfyVaccineSchedulerException(
                    "Customer already exists with email: " + customerDTO.getEmail(), HttpStatus.CONFLICT);
        }
        VaccineCustomer entity = modelMapper.map(customerDTO, VaccineCustomer.class);
        entity.setCustomerId(null);
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("ACTIVE");
        }
        VaccineCustomer saved = customerRepository.save(entity);
        return modelMapper.map(saved, VaccineCustomerDTO.class);
    }

    @Override
    public VaccineCustomerDTO getCustomerById(Integer customerId) throws InfyVaccineSchedulerException {
        VaccineCustomer customer = findExistingCustomer(customerId);
        return modelMapper.map(customer, VaccineCustomerDTO.class);
    }

    @Override
    public List<VaccineCustomerDTO> getAllCustomers() throws InfyVaccineSchedulerException {
        List<VaccineCustomer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            throw new InfyVaccineSchedulerException("No customers found", HttpStatus.NOT_FOUND);
        }
        return customers.stream()
                .map(customer -> modelMapper.map(customer, VaccineCustomerDTO.class))
                .toList();
    }

    @Override
    public VaccineCustomerDTO updateCustomer(Integer customerId, VaccineCustomerDTO customerDTO)
            throws InfyVaccineSchedulerException {
        VaccineCustomer existing = findExistingCustomer(customerId);
        existing.setFirstName(customerDTO.getFirstName());
        existing.setLastName(customerDTO.getLastName());
        existing.setEmail(customerDTO.getEmail());
        existing.setPhoneNumber(customerDTO.getPhoneNumber());
        existing.setDateOfBirth(customerDTO.getDateOfBirth());
        if (customerDTO.getStatus() != null && !customerDTO.getStatus().isBlank()) {
            existing.setStatus(customerDTO.getStatus());
        }
        VaccineCustomer updated = customerRepository.save(existing);
        return modelMapper.map(updated, VaccineCustomerDTO.class);
    }

    @Override
    public String deleteCustomer(Integer customerId) throws InfyVaccineSchedulerException {
        VaccineCustomer existing = findExistingCustomer(customerId);
        customerRepository.delete(existing);
        return "Customer with ID " + customerId + " deleted successfully";
    }

    private VaccineCustomer findExistingCustomer(Integer customerId) throws InfyVaccineSchedulerException {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyVaccineSchedulerException(
                        "Customer not found with ID: " + customerId, HttpStatus.NOT_FOUND));
    }
}
