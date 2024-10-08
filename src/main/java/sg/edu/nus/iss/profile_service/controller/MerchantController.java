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
import sg.edu.nus.iss.profile_service.dto.MerchantDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.*;

@RestController
@RequestMapping("/merchants")
@Tag(name = "Merchants", description = "Manage merchants in Shopsmart Profile Management API")
public class MerchantController {

    @Autowired
    private ProfileServiceFactory profileServiceFactory;

    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    @Operation(summary = "Retrieve all merchants")
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        List<Merchant> merchantList = profileServiceFactory.getProfilesByType("merchant").stream().map(merchant -> (Merchant) merchant).toList();
        return ResponseEntity.ok(merchantList);
    }

    @GetMapping("/{merchantId}")
    @Operation(summary = "Retrieve merchants by ID")
    public ResponseEntity<Merchant> getMerchant(@PathVariable UUID merchantId) {

            Optional<Profile> profile = profileServiceFactory.getProfileById("merchant", merchantId);
            if (profile.isPresent() && profile.get() instanceof Merchant) {
                return ResponseEntity.ok((Merchant) profile.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{merchantId}")
    @Operation(summary = "Update merchants")
    public ResponseEntity<String> updateMerchant(@PathVariable UUID merchantId, @Valid @RequestBody MerchantDTO merchantDTO) {
           Optional<Profile> existingMerchantOpt = profileServiceFactory.getProfileById("merchant", merchantId);
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
        Optional<Profile> existingMerchantOpt = profileServiceFactory.getProfileById("merchant", merchantId);
        if (existingMerchantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Merchant not found");
        }

        Merchant existingMerchant = (Merchant) existingMerchantOpt.get();
        // soft delete
        existingMerchant.setDeleted(true);
        profileServiceFactory.updateProfile(existingMerchant);
        return ResponseEntity.ok("Delete: successful");
    }

    @PostMapping
    @Operation(summary = "Register a new merchant")
    public ResponseEntity<String> registerMerchant(@Valid @RequestBody Merchant merchant) {

        Optional<Profile> merchantByEmail = profileServiceFactory.getProfileByEmailAddress(merchant.getEmailAddress(), "merchant" );
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