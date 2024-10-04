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
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.service.MerchantService;

import java.util.*;

@RestController
@RequestMapping("/merchants")
@Tag(name = "Merchants", description = "Manage merchants in Shopsmart Profile Management API")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    @Operation(summary = "Retrieve all merchants")
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        List<Merchant> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/{merchantId}")
    @Operation(summary = "Retrieve merchants by ID")
    public ResponseEntity<Merchant> getMerchant(@PathVariable UUID merchantId) {
        Optional<Merchant> merchant = merchantService.getMerchant(merchantId);
        return merchant.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{merchantId}")
    @Operation(summary = "Update merchants")
    public ResponseEntity<String> updateMerchant(@PathVariable UUID merchantId, @Valid @RequestBody MerchantDTO merchantDTO) {
        Optional<Merchant> existingMerchantOpt = merchantService.getMerchant(merchantId);

        Merchant merchant = mapper.convertValue(merchantDTO, Merchant.class);
        merchant.setMerchantId(merchantDTO.getMerchantId());

        if (existingMerchantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Merchant not found");
        }

        if(!existingMerchantOpt.get().getMerchantId().equals(merchantDTO.getMerchantId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Merchant ID mismatch");
        }

        if (merchant.getMerchantName().isEmpty() || merchant.getMerchantEmail().isEmpty()) {
            return new ResponseEntity<>("Invalid Merchant data", HttpStatus.BAD_REQUEST);
        }

        Merchant existingMerchant = existingMerchantOpt.get();
        // Check if the merchant name is changed
        if (merchant.getMerchantName() == null || !merchant.getMerchantName().equals(existingMerchant.getMerchantName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Merchant name shouldn't be changed");
        }
        // Check if the email is changed
        if (merchant.getMerchantEmail() == null || !merchant.getMerchantEmail().equals(existingMerchant.getMerchantEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email shouldn't be changed");
        }

        merchantService.updateMerchant(merchant);
        return ResponseEntity.ok("Updated: true");
}

    @DeleteMapping("/{merchantId}")
    @Operation(summary = "Delete merchant by ID")
    public ResponseEntity<String> deleteMerchant(@PathVariable UUID merchantId) {
        Optional<Merchant> existingMerchantOpt = merchantService.getMerchant(merchantId);
        if (existingMerchantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Merchant not found");
        }

        Merchant existingMerchant = existingMerchantOpt.get();
        // soft delete
        existingMerchant.setDeleted(true);
        merchantService.updateMerchant(existingMerchant);
        return ResponseEntity.ok("Delete: successful");
    }

    @PostMapping
    @Operation(summary = "Register a new merchant")
    public ResponseEntity<String> registerMerchant(@Valid @RequestBody Merchant merchant) {
        if (merchant.getMerchantEmail() == null || merchant.getMerchantEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Optional<Merchant> merchantByEmail = merchantService.getMerchantByEmailId(merchant.getMerchantEmail());
        if (merchantByEmail.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
        }
        merchantService.registerMerchant(merchant);
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