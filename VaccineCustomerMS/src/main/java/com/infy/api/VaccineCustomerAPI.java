package com.infy.api;

import com.infy.dto.VaccineCustomerDTO;
import com.infy.exception.InfyVaccineSchedulerException;
import com.infy.service.VaccineCustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vaccine")
@Validated
public class VaccineCustomerAPI {

    private final VaccineCustomerService customerService;

    @Autowired
    public VaccineCustomerAPI(VaccineCustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customers")
    public ResponseEntity<VaccineCustomerDTO> registerCustomer(@Valid @RequestBody VaccineCustomerDTO customerDTO)
            throws InfyVaccineSchedulerException {
        VaccineCustomerDTO saved = customerService.registerCustomer(customerDTO);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<VaccineCustomerDTO>> getAllCustomers() throws InfyVaccineSchedulerException {
        return new ResponseEntity<>(customerService.getAllCustomers(), HttpStatus.OK);
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<VaccineCustomerDTO> getCustomerById(
            @PathVariable @Positive(message = "{customer.id.invalid}") Integer customerId)
            throws InfyVaccineSchedulerException {
        return new ResponseEntity<>(customerService.getCustomerById(customerId), HttpStatus.OK);
    }

    @PutMapping("/customers/{customerId}")
    public ResponseEntity<VaccineCustomerDTO> updateCustomer(
            @PathVariable @Positive(message = "{customer.id.invalid}") Integer customerId,
            @Valid @RequestBody VaccineCustomerDTO customerDTO) throws InfyVaccineSchedulerException {
        return new ResponseEntity<>(customerService.updateCustomer(customerId, customerDTO), HttpStatus.OK);
    }

    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<String> deleteCustomer(
            @PathVariable @Positive(message = "{customer.id.invalid}") Integer customerId)
            throws InfyVaccineSchedulerException {
        return new ResponseEntity<>(customerService.deleteCustomer(customerId), HttpStatus.OK);
    }
}
