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
import sg.edu.nus.iss.profile_service.ProfileServiceApplication;
import sg.edu.nus.iss.profile_service.dto.MerchantDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.*;

@RestController
@RequestMapping("/merchants")
@Tag(name = "Merchants", description = "Manage merchants in Shopsmart Profile Management API")
public class MerchantController {

    private static final Logger log = LoggerFactory.getLogger(MerchantController.class);

    private final ProfileServiceFactory profileServiceFactory;

    private final ObjectMapper mapper;

    private static final String MERCHANT_STRING = "merchant";

    @Autowired
    public MerchantController(ProfileServiceFactory profileServiceFactory, ObjectMapper mapper) {
        this.profileServiceFactory = profileServiceFactory;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve all merchants")
    public ResponseEntity<?> getAllMerchants(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {


        if (page == null || size == null) {
            log.info("Fetching all merchants with no pagination");
            List<Merchant> merchantList = profileServiceFactory.getProfilesByType(MERCHANT_STRING).stream()
                    .map(Merchant.class::cast)
                    .toList();
            return ResponseEntity.ok(merchantList);
        }


        log.info("Fetching page {} of size {}", page, size);


        // If pagination parameters are provided, return a page of merchants
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> merchantPage = profileServiceFactory.getProfilesWithPagination(MERCHANT_STRING, pageable);

        return ResponseEntity.ok(merchantPage);
    }

    @GetMapping("/{merchantId}")
    @Operation(summary = "Retrieve merchants by ID")
    public ResponseEntity<Merchant> getMerchant(@PathVariable UUID merchantId) {

          log.info("Fetching merchant with ID: {}", merchantId);
            Optional<Profile> profile = profileServiceFactory.getProfileById(MERCHANT_STRING, merchantId);
            if (profile.isPresent() && profile.get() instanceof Merchant merchant) {
                return ResponseEntity.ok(merchant);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{merchantId}")
    @Operation(summary = "Update merchants")
    public ResponseEntity<String> updateMerchant(@PathVariable UUID merchantId, @Valid @RequestBody MerchantDTO merchantDTO) {

        log.info("Updating merchant with ID: {}", merchantId);

               // Check if the merchant exists
        Optional<Profile> existingMerchantOpt = profileServiceFactory.getProfileById(MERCHANT_STRING, merchantId);
               if (existingMerchantOpt.isEmpty()) {
                   return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Merchant not found");
               }

               Merchant existingMerchant = (Merchant) existingMerchantOpt.get();

               // Check if the Merchant ID matches
               if (!existingMerchant.getMerchantId().equals(merchantDTO.getMerchantId())) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Merchant ID mismatch");
               }

               // Ensure merchant name and email haven't changed
               if (!merchantDTO.getName().equals(existingMerchant.getName())) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Merchant name shouldn't be changed");
               }

               if (!merchantDTO.getEmailAddress().equals(existingMerchant.getEmailAddress())) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email shouldn't be changed");
               }

               // Convert MerchantDTO to Merchant entity
               Merchant merchant = mapper.convertValue(merchantDTO, Merchant.class);
               merchant.setMerchantId(merchantDTO.getMerchantId()); // Set the merchantId from the DTO

               // Update and save the merchant
               profileServiceFactory.updateProfile(merchant);

               return ResponseEntity.ok("Merchant updated successfully");
}

    @DeleteMapping("/{merchantId}")
    @Operation(summary = "Delete merchant by ID")
    public ResponseEntity<String> deleteMerchant(@PathVariable UUID merchantId) {

        log.info("Deleting merchant with ID: {}", merchantId);
        profileServiceFactory.deleteProfile(merchantId);
        return ResponseEntity.ok("Delete: successful");
    }

    @PostMapping
    @Operation(summary = "Register a new merchant")
    public ResponseEntity<String> registerMerchant(@Valid @RequestBody Merchant merchant) {

        log.info("Registering new merchant: {}", merchant);
        Optional<Profile> merchantByEmail = profileServiceFactory.getProfileByEmailAddress(merchant.getEmailAddress(), MERCHANT_STRING );
        if (merchantByEmail.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
        }
        profileServiceFactory.createProfile(merchant);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created Merchant");
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