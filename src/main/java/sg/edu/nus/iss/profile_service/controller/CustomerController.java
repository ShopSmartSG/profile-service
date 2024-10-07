package sg.edu.nus.iss.profile_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.profile_service.dto.CustomerDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.*;

@RestController
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Manage customers in Shopsmart Profile Management API")
public class CustomerController {

    @Autowired
    private ProfileServiceFactory profileServiceFactory;

    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    @Operation(summary = "Retrieve all customer")
    public ResponseEntity<List<Customer>> getAllcustomers() {
        List<Customer> customerList = profileServiceFactory.getProfilesByType("customer").stream().map(customer -> (Customer) customer).toList();
        return ResponseEntity.ok(customerList);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Retrieve customers by ID")
    public ResponseEntity<Customer> getcustomer(@PathVariable UUID customerId) {

        Optional<Profile> profile = profileServiceFactory.getProfileById("customer", customerId);
        if (profile.isPresent() && profile.get() instanceof Customer) {
            return ResponseEntity.ok((Customer) profile.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Update customers")
    public ResponseEntity<String> updateCustomer(@PathVariable UUID customerId, @Valid @RequestBody CustomerDTO customerDTO) {
        Optional<Profile> existingCustomerOpt =profileServiceFactory.getProfileById("customer", customerId);

        Customer customer = mapper.convertValue(customerDTO, Customer.class);
        customer.setCustomerId(customerDTO.getCustomerId());

        if (existingCustomerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("customer not found");
        }

        if(!((Customer) existingCustomerOpt.get()).getCustomerId().equals(customerDTO.getCustomerId())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("customer ID mismatch");
        }

        if (customer.getName().isEmpty() || customer.getEmailAddress().isEmpty()) {
            return new ResponseEntity<>("Invalid customer data", HttpStatus.BAD_REQUEST);
        }

        Customer existingcustomer = (Customer) existingCustomerOpt.get();
        // Check if the customer name is changed
        if (customer.getName() == null || !customer.getName().equals(existingcustomer.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("customer name shouldn't be changed");
        }
        // Check if the email is changed
        if (customer.getEmailAddress() == null || !customer.getEmailAddress().equals(existingcustomer.getEmailAddress())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email shouldn't be changed");
        }

        profileServiceFactory.updateProfile(customer);
        return ResponseEntity.ok("Updated: true");
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete customer by ID")
    public ResponseEntity<String> deleteCustomer(@PathVariable UUID customerId) {
        Optional<Profile> existingCustomerOpt =profileServiceFactory.getProfileById("customer", customerId);
        if (existingCustomerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("customer not found");
        }

        Customer existingcustomer = (Customer) existingCustomerOpt.get();
        // soft delete
        existingcustomer.setDeleted(true);
        profileServiceFactory.updateProfile(existingcustomer);
        return ResponseEntity.ok("Delete: successful");
    }

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody Customer customer) {
        if (customer.getEmailAddress() == null || customer.getEmailAddress().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Optional<Profile> customerByEmail = profileServiceFactory.getProfileByEmailAddress(customer.getEmailAddress(), "merchant" );
        if (customerByEmail.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
        }
        profileServiceFactory.createProfile(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created customer");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}