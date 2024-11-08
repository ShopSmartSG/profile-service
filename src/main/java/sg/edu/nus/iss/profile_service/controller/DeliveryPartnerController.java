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
import sg.edu.nus.iss.profile_service.dto.DeliveryPartnerDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.DeliveryPartner;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.*;

@RestController
@RequestMapping("/partners")
@Tag(name = "partners", description = "Manage delivery partners in Shopsmart Profile Management API")
public class DeliveryPartnerController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryPartnerController.class);

    private final ProfileServiceFactory profileServiceFactory;

    private final ObjectMapper mapper;

    private static final String DELIVERY_STRING = "deliveryPartner";

    @Autowired
    public DeliveryPartnerController(ProfileServiceFactory profileServiceFactory, ObjectMapper mapper) {
        this.profileServiceFactory = profileServiceFactory;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve all delivery partners profile")
    public ResponseEntity<?> getAllDeliveryPartners(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {


        if (page == null || size == null) {
            log.info("{\"message\": \"Fetching all delivery partners with no pagination"+"\"}");
            List<DeliveryPartner> deliveryPartnerList = profileServiceFactory.getProfilesByType(DELIVERY_STRING).stream()
                    .map(DeliveryPartner.class::cast)
                    .toList();
            return ResponseEntity.ok(deliveryPartnerList);
        }


        log.info("{\"message\": \"Fetching all delivery partners with pagination\"}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> deliveryPartnerPage = profileServiceFactory.getProfilesWithPagination(DELIVERY_STRING, pageable);

        return ResponseEntity.ok(deliveryPartnerPage);
    }

    @GetMapping("/{partner-id}")
    @Operation(summary = "Retrieve delivery partner profile by ID")
    public ResponseEntity<DeliveryPartner> getDeliveryPartnerByEmail(@PathVariable(name = "partner-id") UUID deliveryPartnerId) {

        log.info("{\"message\": \"Fetching delivery partner with ID: " + deliveryPartnerId + "\"}");
            Optional<Profile> profile = profileServiceFactory.getProfileById(DELIVERY_STRING, deliveryPartnerId);
            if (profile.isPresent() && profile.get() instanceof DeliveryPartner) {
                DeliveryPartner deliveryPartner = (DeliveryPartner) profile.get();
                return ResponseEntity.ok(deliveryPartner);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{partner-id}")
    @Operation(summary = "Update delivery partner profile")
    public ResponseEntity<String> updateDeliveryPartner(@PathVariable(name = "partner-id") UUID deliveryPartnerId, @Valid @RequestBody DeliveryPartnerDTO deliveryPartnerDTO) {

        log.info("{\"message\": \"Updating delivery partner with ID: {}\"}", deliveryPartnerId);
        Optional<Profile> existingDeliveryPartnerOpt = profileServiceFactory.getProfileById(DELIVERY_STRING, deliveryPartnerId);
               if (existingDeliveryPartnerOpt.isEmpty()) {
                   return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Delivery Partner not found");
               }

               DeliveryPartner existingDeliveryPartner = (DeliveryPartner) existingDeliveryPartnerOpt.get();
               if (!existingDeliveryPartner.getDeliveryPartnerId().equals(deliveryPartnerDTO.getDeliveryPartnerId())) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Delivery Partner Id mismatch");
               }

               // Ensure delivery Partner name and email haven't changed
               if (!deliveryPartnerDTO.getName().equals(existingDeliveryPartner.getName())) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Delivery Partner name shouldn't be changed");
               }

               if (!deliveryPartnerDTO.getEmailAddress().equals(existingDeliveryPartner.getEmailAddress())) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email shouldn't be changed");
               }

        DeliveryPartner deliveryPartner = mapper.convertValue(deliveryPartnerDTO, DeliveryPartner.class);
        deliveryPartner.setDeliveryPartnerId(deliveryPartnerDTO.getDeliveryPartnerId());
               profileServiceFactory.updateProfile(deliveryPartner);

               return ResponseEntity.ok("Delivery Partner updated successfully");
}

    @DeleteMapping("/{partner-id}")
    @Operation(summary = "Delete delivery partner profile by ID")
    public ResponseEntity<String> deleteDeliveryPartner(@PathVariable(name = "partner-id") UUID deliveryPartnerId) {

        log.info("{\"message\": \"Deleting delivery partner with ID: {}\"}", deliveryPartnerId);
        profileServiceFactory.deleteProfile(deliveryPartnerId);
        return ResponseEntity.ok("Delete: successful");
    }

    @PutMapping("/blacklist/{partner-id}")
    @Operation(summary = "Blacklist a Delivery Partner")
    public ResponseEntity<String> blacklistDeliveryPartner(@PathVariable(name = "partner-id") UUID deliveryPartnerId) {
        profileServiceFactory.blacklistProfile(deliveryPartnerId);
        return ResponseEntity.ok("Delivery Partner blacklisted successfully");
    }

    @PutMapping("/unblacklist/{partner-id}")
    @Operation(summary = "Unblacklist a Delivery Partner")
    public ResponseEntity<String> unblacklistDeliveryPartner(@PathVariable(name = "partner-id") UUID deliveryPartnerId) {
        profileServiceFactory.unblacklistProfile(deliveryPartnerId);
        return ResponseEntity.ok("Delivery Partner unblacklisted successfully");
    }



    @PostMapping
    @Operation(summary = "Register a new Delivery Partner Profile")
    public ResponseEntity<String> registerDeliveryPartner(@Valid @RequestBody DeliveryPartner deliveryPartner) {

        log.info("{\"message\": \"Registering new deliveryPartner\"}", deliveryPartner);
        Optional<Profile> deliveryPartnerByEmail = profileServiceFactory.getProfileByEmailAddress(deliveryPartner.getEmailAddress(), DELIVERY_STRING );
        if (deliveryPartnerByEmail.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
        }
        profileServiceFactory.createProfile(deliveryPartner);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created Delivery Partner");
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Retrieve Delivery Partner by email address")
    public ResponseEntity<?> getDeliveryPartnerByEmail(@PathVariable String email) {
        log.info("{\"message\": \"Fetching delivery partner with email: {}\"}", email);
        Optional<Profile> profile = profileServiceFactory.getProfileByEmailAddress(email, DELIVERY_STRING);
        if (profile.isPresent() && profile.get() instanceof DeliveryPartner ) {
            DeliveryPartner deliveryPartner = (DeliveryPartner) profile.get();
            log.info("{\"message\": \"Found delivery partner with email: {}\"}", email);
            return ResponseEntity.ok(deliveryPartner.getDeliveryPartnerId());
        }
        log.error("{\"message\": \"Couldn't fine delivery partner with email: {}\"}", email);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Delivery partner not found");
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