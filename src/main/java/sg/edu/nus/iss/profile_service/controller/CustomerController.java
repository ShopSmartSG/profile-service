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
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.*;

@RestController
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Manage customers in Shopsmart Profile Management via APIs")
public class CustomerController {

    private final ProfileServiceFactory profileServiceFactory;

    private final ObjectMapper mapper;

    private static final String CUSTOMER_TYPE = "customer";

    @Autowired
    public CustomerController(ProfileServiceFactory profileServiceFactory, ObjectMapper mapper) {
        this.profileServiceFactory = profileServiceFactory;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve all customer")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customerList = profileServiceFactory.getProfilesByType(CUSTOMER_TYPE).stream().map(Customer.class::cast).toList();
        return ResponseEntity.ok(customerList);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Retrieve customers by ID")
    public ResponseEntity<Customer> getCustomer(@PathVariable UUID customerId) {

        Optional<Profile> profile = profileServiceFactory.getProfileById(CUSTOMER_TYPE, customerId);
        if (profile.isPresent() && profile.get() instanceof Customer customer) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Update customers")
    public ResponseEntity<String> updateCustomer(@PathVariable UUID customerId, @Valid @RequestBody CustomerDTO customerDTO) {
            Optional<Profile> existingCustomerOpt = profileServiceFactory.getProfileById(CUSTOMER_TYPE, customerId);
            if (existingCustomerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
            }

            Customer existingCustomer = (Customer) existingCustomerOpt.get();

            // Check if the Customer ID matches
            if (!existingCustomer.getCustomerId().equals(customerDTO.getCustomerId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Customer ID mismatch");
            }

            // Ensure customer name and email haven't changed
            if (!customerDTO.getName().equals(existingCustomer.getName())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Customer name shouldn't be changed");
            }

            if (!customerDTO.getEmailAddress().equals(existingCustomer.getEmailAddress())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email shouldn't be changed");
            }

            // Convert CustomerDTO to Customer entity
            Customer customer = mapper.convertValue(customerDTO, Customer.class);
            customer.setCustomerId(customerDTO.getCustomerId()); // Set the customerId from the DTO

            // Update and save the customer
            profileServiceFactory.updateProfile(customer);

            return ResponseEntity.ok("Customer updated successfully");
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete customer by ID")
    public ResponseEntity<String> deleteCustomer(@PathVariable UUID customerId) {
        profileServiceFactory.deleteProfile(customerId);
        return ResponseEntity.ok("Delete: successful");
    }

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody Customer customer) {
            Optional<Profile> customerByEmail = profileServiceFactory.getProfileByEmailAddress(customer.getEmailAddress(), CUSTOMER_TYPE);
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