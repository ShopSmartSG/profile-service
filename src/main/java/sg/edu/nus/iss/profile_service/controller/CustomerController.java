package sg.edu.nus.iss.profile_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.profile_service.dto.CustomerDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.Profile;
import sg.edu.nus.iss.profile_service.model.Rewards;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Manage customers in Shopsmart Profile Management via APIs")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final ProfileServiceFactory profileServiceFactory;

    private final ObjectMapper mapper;

    private static final String CUSTOMER_TYPE = "customer";

    @Autowired
    public CustomerController(ProfileServiceFactory profileServiceFactory, ObjectMapper mapper) {
        this.profileServiceFactory = profileServiceFactory;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve all customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        // If pagination parameters are not provided, return list of customers
        if (page == null || size == null) {
            log.info("{\"message\": \"Fetching all customers with no pagination"+"\"}");
            List<Customer> customerList = profileServiceFactory.getProfilesByType(CUSTOMER_TYPE).stream()
                    .map(Customer.class::cast)
                    .toList();
            return ResponseEntity.ok(customerList);
        }

        // If pagination parameters are provided, return a page of customers
        Pageable pageable = PageRequest.of(page, size);
        log.info("{\"message\": \"Fetching customers with pagination attributes: page {} and size {} "+page +","+size+ "\"}");
        Page<Profile> customerPage = profileServiceFactory.getProfilesWithPagination(CUSTOMER_TYPE, pageable);

        return ResponseEntity.ok(customerPage);
    }

    @GetMapping("/{customer-id}")
    @Operation(summary = "Retrieve customers by ID")
    public ResponseEntity<Customer> getCustomer(@PathVariable(name = "customer-id") UUID customerId) {

        log.info("{\"message\": \"Fetching customer with ID: {}\"}", customerId);

        Optional<Profile> profile = profileServiceFactory.getProfileById(CUSTOMER_TYPE, customerId);
        if (profile.isPresent() && profile.get() instanceof Customer ) {
            Customer customer = (Customer) profile.get();
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{customer-id}")
    @Operation(summary = "Update customers")
    public ResponseEntity<String> updateCustomer(@PathVariable(name = "customer-id") UUID customerId, @Valid @RequestBody CustomerDTO customerDTO) {
            Optional<Profile> existingCustomerOpt = profileServiceFactory.getProfileById(CUSTOMER_TYPE, customerId);
        log.info("{\"message\": \"Updating customer with ID: {}\"}", customerId);
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

    @DeleteMapping("/{customer-id}")
    @Operation(summary = "Delete customer by ID")
    public ResponseEntity<String> deleteCustomer(@PathVariable(name = "customer-id") UUID customerId) {
        log.info("{\"message\": \"Deleting customer with ID: {}\"}", customerId);
        profileServiceFactory.deleteProfile(customerId);
        return ResponseEntity.ok("Delete: successful");
    }

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody Customer customer) {
        log.info("{\"message\": \"Registering new customer: {}\"}", customer);
            Optional<Profile> customerByEmail = profileServiceFactory.getProfileByEmailAddress(customer.getEmailAddress(), CUSTOMER_TYPE);
            if (customerByEmail.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
            }
            profileServiceFactory.createProfile(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body("Created customer");
    }

    // add an API to get profile by email address

    @GetMapping("/email/{email}")
    @Operation(summary = "Retrieve customer by email address")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        log.info("{\"message\": \"Fetching customer with email: {}\"}", email);
        Optional<Profile> profile = profileServiceFactory.getProfileByEmailAddress(email, CUSTOMER_TYPE);
        if (profile.isPresent() && profile.get() instanceof Customer ) {
            Customer customer = (Customer) profile.get();
            log.info("{\"message\": \"Found customer with email: {}\"}", email);
            return ResponseEntity.ok(customer.getCustomerId());
        }
        log.error("{\"message\": \"Couldn't fine customer with email: {}\"}", email);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
    }

    @PutMapping("/{customer-id}/rewards/{order-price}")
    @Operation(summary = "Update Customer Reward Points" , description = "Customer Order Value = Number of Reward Points")
    public ResponseEntity<?> patchRewardPoints(@PathVariable(name = "customer-id") UUID customerId ,@PathVariable("order-price") BigDecimal amount){

        Optional<Profile> profile = profileServiceFactory.getProfileById(CUSTOMER_TYPE,customerId);
        if(profile.isPresent() && profile.get() instanceof Customer ){
            Customer customer = (Customer) profile.get();
            // get order price and set reward points
            // 100 -> 100
            customer.setRewardPoints(BigDecimal.valueOf(amount.doubleValue()));
            profileServiceFactory.updateProfile(customer);
            return ResponseEntity.ok("Reward points updated successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Not found");
    }


    @GetMapping("/{customer-id}/rewards")
    @Operation(summary = "get reward points and reward amount equivalent for a customer" , description = "100 Reward points = 1 S$")
    public ResponseEntity<Rewards> retrieveRewardDetails(@PathVariable(name = "customer-id") UUID customerId) {
        // get points give back amount
        // 100 reward points to 10$
        try {
            Optional<Profile> profile = profileServiceFactory.getProfileById(CUSTOMER_TYPE, customerId);
            if (profile.isPresent() && profile.get() instanceof Customer ) {
                Customer customer = (Customer) profile.get();
                Rewards rewards = new Rewards();
                // set reward points as is
                rewards.setRewardPoints(customer.getRewardPoints());
                // reward points are stored at customer , reward points to reward amount coversion is to done here
                rewards.setRewardAmount(rewards.getRewardPoints().multiply(BigDecimal.valueOf(0.010)));
                return ResponseEntity.ok(rewards);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().build();
        }

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