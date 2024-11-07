package sg.edu.nus.iss.profile_service.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import sg.edu.nus.iss.profile_service.dto.MerchantDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.math.BigDecimal;
import java.util.*;

public class MerchantControllerTest {

    @Mock
    private ProfileServiceFactory profileServiceFactory;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private MerchantController merchantController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test getAllmerchants without pagination
    @Test
    public void testGetAllMerchantsWithoutPagination() {
        List<Profile> merchants = List.of(new Merchant());
        when(profileServiceFactory.getProfilesByType("merchant")).thenReturn(merchants);

        ResponseEntity<?> response = merchantController.getAllMerchants(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(merchants, response.getBody());
        verify(profileServiceFactory, times(1)).getProfilesByType("merchant");
    }

    // Test getmerchantById - merchant found
    @Test
    public void testGetMerchantById_Success() {
        Merchant merchant = new Merchant();
        when(profileServiceFactory.getProfileById(eq("merchant"), any(UUID.class))).thenReturn(Optional.of(merchant));

        ResponseEntity<?> response = merchantController.getMerchant(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(merchant, response.getBody());
    }

    // Test getmerchantById - merchant not found
    @Test
    public void testGetMerchantById_NotFound() {
        when(profileServiceFactory.getProfileById(eq("merchant"), any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<?> response = merchantController.getMerchant(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test updatemerchant - success
    @Test
    public void testUpdateMerchant_Success() {
        UUID merchantId = UUID.randomUUID();
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setMerchantId(merchantId);
        merchantDTO.setEmailAddress("test@example.com");
        merchantDTO.setName("test");

        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setEmailAddress("test@example.com");
        merchant.setName("test");
        when(profileServiceFactory.getProfileById(eq("merchant"), eq(merchantDTO.getMerchantId()))).thenReturn(Optional.of(merchant));
        when(mapper.convertValue(any(MerchantDTO.class), eq(Merchant.class))).thenReturn(merchant);

        ResponseEntity<?> response = merchantController.updateMerchant(merchantDTO.getMerchantId(), merchantDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Merchant updated successfully", response.getBody());
        verify(profileServiceFactory, times(1)).updateProfile(merchant);
    }

    // Test updatemerchant - email mismatch
    @Test
    public void testUpdatemerchant_EmailMismatch() {
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setMerchantId(UUID.randomUUID());
        merchantDTO.setEmailAddress("new@example.com");
        merchantDTO.setName("new");

        Merchant merchant = new Merchant();
        merchant.setName("new");
        merchant.setEmailAddress("old@example.com");
        merchant.setMerchantId(merchantDTO.getMerchantId());
        when(profileServiceFactory.getProfileById(eq("merchant"), eq(merchantDTO.getMerchantId()))).thenReturn(Optional.of(merchant));

        ResponseEntity<?> response = merchantController.updateMerchant(merchantDTO.getMerchantId(), merchantDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email shouldn't be changed", response.getBody());
    }

    // Test deletemerchant - success
    @Test
    public void testDeleteMerchant_Success() {
        UUID merchantId = UUID.randomUUID();

        ResponseEntity<String> response = merchantController.deleteMerchant(merchantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete: successful", response.getBody());
        verify(profileServiceFactory, times(1)).deleteProfile(merchantId);
    }

    // Test registermerchant - success
    @Test
    public void testRegistermerchant_Success() {
        Merchant merchant = new Merchant();
        merchant.setEmailAddress("test@example.com");

        when(profileServiceFactory.getProfileByEmailAddress(merchant.getEmailAddress(), "merchant")).thenReturn(Optional.empty());

        ResponseEntity<String> response = merchantController.registerMerchant(merchant);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Created Merchant", response.getBody());
        verify(profileServiceFactory, times(1)).createProfile(merchant);
    }

    // Test registermerchant - email already registered
    @Test
    public void testRegistermerchant_EmailAlreadyRegistered() {
        Merchant merchant = new Merchant();
        merchant.setEmailAddress("test@example.com");

        when(profileServiceFactory.getProfileByEmailAddress(merchant.getEmailAddress(), "merchant")).thenReturn(Optional.of(new Merchant()));

        ResponseEntity<String> response = merchantController.registerMerchant(merchant);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }

    // Test validation error handling
    @Test
    public void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("merchant", "email", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        Map<String, String> errors = merchantController.handleValidationExceptions(ex);

        assertEquals(1, errors.size());
        assertEquals("must not be null", errors.get("email"));
    }

    // Test blacklisting a merchant - success
    @Test
    public void testBlacklistMerchant_Success() {
        UUID merchantId = UUID.randomUUID();

        ResponseEntity<String> response = merchantController.blacklistMerchant(merchantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Merchant blacklisted successfully", response.getBody());
        verify(profileServiceFactory, times(1)).blacklistProfile(merchantId);
    }

    // Test unblacklisting a merchant - success
    @Test
    public void testUnblacklistMerchant_Success() {
        UUID merchantId = UUID.randomUUID();

        ResponseEntity<String> response = merchantController.unblacklistMerchant(merchantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Merchant unblacklisted successfully", response.getBody());
        verify(profileServiceFactory, times(1)).unblacklistProfile(merchantId);
    }


    // Test getMerchantByEmail - merchant found
    @Test
    public void testGetMerchantByEmail_Success() {
        String email = "test@example.com";
        Merchant merchant = new Merchant();
        when(profileServiceFactory.getProfileByEmailAddress(email, "merchant")).thenReturn(Optional.of(merchant));

        ResponseEntity<?> response = merchantController.getMerchantByEmail(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(merchant.getMerchantId(), response.getBody());
    }


    // Test patchMerchantEarnings
    @Test
    public void testPatchMerchantEarnings_Success() {
        UUID merchantId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setEarnings(BigDecimal.valueOf(100));
        when(profileServiceFactory.getProfileById("merchant", merchantId)).thenReturn(Optional.of(merchant));

        ResponseEntity<?> response = merchantController.patchMerchantEarnings(merchantId, amount);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Merchant Earnings updated successfully", response.getBody());
        verify(profileServiceFactory, times(1)).updateProfile(merchant);
    }
}