package sg.edu.nus.iss.profile_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.service.MerchantService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MerchantControllerTest {

    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private MerchantController merchantController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMerchants() {
        List<Merchant> merchants = new ArrayList<>();
        merchants.add(new Merchant());
        when(merchantService.getAllMerchants()).thenReturn(merchants);

        ResponseEntity<List<Merchant>> response = merchantController.getAllMerchants();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(merchants, response.getBody());
    }

    @Test
    void testGetMerchant() {
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant();
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.of(merchant));

        ResponseEntity<Merchant> response = merchantController.getMerchant(merchantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(merchant, response.getBody());
    }

    @Test
    void testGetMerchantNotFound() {
        UUID merchantId = UUID.randomUUID();
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.empty());

        ResponseEntity<Merchant> response = merchantController.getMerchant(merchantId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateMerchant() {
        UUID merchantId = UUID.randomUUID();
        Merchant existingMerchant = new Merchant();
        existingMerchant.setMerchantName("Existing Name");
        existingMerchant.setMerchantEmail("existing@example.com");
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.of(existingMerchant));

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantName("Existing Name");
        updatedMerchant.setMerchantEmail("existing@example.com");

        ResponseEntity<String> response = merchantController.updateMerchant(merchantId, updatedMerchant);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated: true", response.getBody());
    }

    @Test
    void testUpdateMerchantNotFound() {
        UUID merchantId = UUID.randomUUID();
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.empty());

        Merchant updatedMerchant = new Merchant();

        ResponseEntity<String> response = merchantController.updateMerchant(merchantId, updatedMerchant);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Merchant not found", response.getBody());
    }

    @Test
    void testUpdateMerchantNameChanged() {
        UUID merchantId = UUID.randomUUID();
        Merchant existingMerchant = new Merchant();
        existingMerchant.setMerchantName("Existing Name");
        existingMerchant.setMerchantEmail("existing@example.com");
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.of(existingMerchant));

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantName("New Name");
        updatedMerchant.setMerchantEmail("existing@example.com");

        ResponseEntity<String> response = merchantController.updateMerchant(merchantId, updatedMerchant);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Merchant name shouldn't be changed", response.getBody());
    }

    @Test
    void testUpdateMerchantEmailChanged() {
        UUID merchantId = UUID.randomUUID();
        Merchant existingMerchant = new Merchant();
        existingMerchant.setMerchantName("Existing Name");
        existingMerchant.setMerchantEmail("existing@example.com");
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.of(existingMerchant));

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantName("Existing Name");
        updatedMerchant.setMerchantEmail("new@example.com");

        ResponseEntity<String> response = merchantController.updateMerchant(merchantId, updatedMerchant);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email shouldn't be changed", response.getBody());
    }

    @Test
    void testDeleteMerchant() {
        UUID merchantId = UUID.randomUUID();
        Merchant existingMerchant = new Merchant();
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.of(existingMerchant));

        ResponseEntity<String> response = merchantController.deleteMerchant(merchantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete: successful", response.getBody());
    }

    @Test
    void testDeleteMerchantNotFound() {
        UUID merchantId = UUID.randomUUID();
        when(merchantService.getMerchant(merchantId)).thenReturn(Optional.empty());

        ResponseEntity<String> response = merchantController.deleteMerchant(merchantId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Merchant not found", response.getBody());
    }

    @Test
    void testRegisterMerchant() {
        Merchant newMerchant = new Merchant();
        newMerchant.setMerchantEmail("new@example.com");
        when(merchantService.getMerchantByEmailId("new@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = merchantController.registerMerchant(newMerchant);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Created Merchant", response.getBody());
    }

    @Test
    void testRegisterMerchantEmailExists() {
        Merchant newMerchant = new Merchant();
        newMerchant.setMerchantEmail("existing@example.com");
        when(merchantService.getMerchantByEmailId("existing@example.com")).thenReturn(Optional.of(new Merchant()));

        ResponseEntity<String> response = merchantController.registerMerchant(newMerchant);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }


    @Test
    void testRegisterMerchantInvalidEmail() {
        Merchant newMerchant = new Merchant();
        newMerchant.setMerchantEmail("");

        ResponseEntity<String> response = merchantController.registerMerchant(newMerchant);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void testDeleteMerchantInvalidId() {
        UUID merchantId = UUID.randomUUID();

        ResponseEntity<String> response = merchantController.deleteMerchant(merchantId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetMerchantInvalidId() {
        UUID merchantId = UUID.randomUUID();

        ResponseEntity<Merchant> response = merchantController.getMerchant(merchantId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}