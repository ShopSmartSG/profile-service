package sg.edu.nus.iss.profile_service.controller;

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
import sg.edu.nus.iss.profile_service.dto.DeliveryPartnerDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.DeliveryPartner;
import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DeliveryPartnerControllerTest {

    @Mock
    private ProfileServiceFactory profileServiceFactory;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private DeliveryPartnerController deliveryPartnerController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test getAlldeliveryPartners without pagination
    @Test
    public void testGetAlldeliveryPartnersWithoutPagination() {
        List<Profile> deliveryPartners = List.of(new DeliveryPartner());
        when(profileServiceFactory.getProfilesByType("deliveryPartner")).thenReturn(deliveryPartners);

        ResponseEntity<?> response = deliveryPartnerController.getAllDeliveryPartners(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deliveryPartners, response.getBody());
        verify(profileServiceFactory, times(1)).getProfilesByType("deliveryPartner");
    }

    // Test getdeliveryPartnerById - deliveryPartner found
    @Test
    public void testGetdeliveryPartnerById_Success() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        when(profileServiceFactory.getProfileById(eq("deliveryPartner"), any(UUID.class))).thenReturn(Optional.of(deliveryPartner));

        ResponseEntity<?> response = deliveryPartnerController.getDeliveryPartnerByEmail(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deliveryPartner, response.getBody());
    }

    // Test getdeliveryPartnerById - deliveryPartner not found
    @Test
    public void testGetdeliveryPartnerById_NotFound() {
        when(profileServiceFactory.getProfileById(eq("deliveryPartner"), any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<?> response = deliveryPartnerController.getDeliveryPartnerByEmail(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test updatedeliveryPartner - success
    @Test
    public void testUpdatedeliveryPartner_Success() {
        UUID deliveryPartnerId = UUID.randomUUID();
        DeliveryPartnerDTO deliveryPartnerDTO = new DeliveryPartnerDTO();
        deliveryPartnerDTO.setDeliveryPartnerId(deliveryPartnerId);
        deliveryPartnerDTO.setEmailAddress("test@example.com");
        deliveryPartnerDTO.setName("test");

        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setDeliveryPartnerId(deliveryPartnerId);
        deliveryPartner.setEmailAddress("test@example.com");
        deliveryPartner.setName("test");
        when(profileServiceFactory.getProfileById(eq("deliveryPartner"), eq(deliveryPartnerDTO.getDeliveryPartnerId()))).thenReturn(Optional.of(deliveryPartner));
        when(mapper.convertValue(any(DeliveryPartnerDTO.class), eq(DeliveryPartner.class))).thenReturn(deliveryPartner);

        ResponseEntity<?> response = deliveryPartnerController.updateDeliveryPartner(deliveryPartnerDTO.getDeliveryPartnerId(), deliveryPartnerDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delivery Partner updated successfully", response.getBody());
        verify(profileServiceFactory, times(1)).updateProfile(deliveryPartner);
    }

    // Test updatedeliveryPartner - email mismatch
    @Test
    public void testUpdatedeliverypartner_EmailMismatch() {
        DeliveryPartnerDTO deliveryPartnerDTO = new DeliveryPartnerDTO();
        deliveryPartnerDTO.setDeliveryPartnerId(UUID.randomUUID());
        deliveryPartnerDTO.setEmailAddress("new@example.com");
        deliveryPartnerDTO.setName("new");

        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setName("new");
        deliveryPartner.setEmailAddress("old@example.com");
        deliveryPartner.setDeliveryPartnerId(deliveryPartnerDTO.getDeliveryPartnerId());
        when(profileServiceFactory.getProfileById(eq("deliveryPartner"), eq(deliveryPartnerDTO.getDeliveryPartnerId()))).thenReturn(Optional.of(deliveryPartner));

        ResponseEntity<?> response = deliveryPartnerController.updateDeliveryPartner(deliveryPartnerDTO.getDeliveryPartnerId(), deliveryPartnerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email shouldn't be changed", response.getBody());
    }

    // Test deletedeliveryPartner - success
    @Test
    public void testDeletePartner_Success() {
        UUID deliveryPartnerId = UUID.randomUUID();

        ResponseEntity<String> response = deliveryPartnerController.deleteDeliveryPartner(deliveryPartnerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete: successful", response.getBody());
        verify(profileServiceFactory, times(1)).deleteProfile(deliveryPartnerId);
    }

    // Test registerdeliveryPartner - success
    @Test
    public void testRegisterPartner_Success() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setEmailAddress("test@example.com");

        when(profileServiceFactory.getProfileByEmailAddress(deliveryPartner.getEmailAddress(), "deliveryPartner")).thenReturn(Optional.empty());

        ResponseEntity<String> response = deliveryPartnerController.registerDeliveryPartner(deliveryPartner);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Created Delivery Partner", response.getBody());
        verify(profileServiceFactory, times(1)).createProfile(deliveryPartner);
    }

    // Test registerdeliveryPartner - email already registered
    @Test
    public void testRegisterPartner_EmailAlreadyRegistered() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setEmailAddress("test@example.com");

        when(profileServiceFactory.getProfileByEmailAddress(deliveryPartner.getEmailAddress(), "deliveryPartner")).thenReturn(Optional.of(new DeliveryPartner()));

        ResponseEntity<String> response = deliveryPartnerController.registerDeliveryPartner(deliveryPartner);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }

    // Test validation error handling
    @Test
    public void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("deliveryPartner", "email", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        Map<String, String> errors = deliveryPartnerController.handleValidationExceptions(ex);

        assertEquals(1, errors.size());
        assertEquals("must not be null", errors.get("email"));
    }

    // Test blacklisting a deliveryPartner - success
    @Test
    public void testBlacklistPartner_Success() {
        UUID deliveryPartnerId = UUID.randomUUID();

        ResponseEntity<String> response = deliveryPartnerController.blacklistDeliveryPartner(deliveryPartnerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delivery Partner blacklisted successfully", response.getBody());
        verify(profileServiceFactory, times(1)).blacklistProfile(deliveryPartnerId);
    }

    // Test unblacklisting a deliveryPartner - success
    @Test
    public void testUnblacklistPartner_Success() {
        UUID deliveryPartnerId = UUID.randomUUID();

        ResponseEntity<String> response = deliveryPartnerController.unblacklistDeliveryPartner(deliveryPartnerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delivery Partner unblacklisted successfully", response.getBody());
        verify(profileServiceFactory, times(1)).unblacklistProfile(deliveryPartnerId);
    }


    // Test getdeliveryPartnerByEmail - deliveryPartner found
    @Test
    public void testGetdeliveryPartnerByEmail_Success() {
        String email = "test@example.com";
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        when(profileServiceFactory.getProfileByEmailAddress(email, "deliveryPartner")).thenReturn(Optional.of(deliveryPartner));

        ResponseEntity<?> response = deliveryPartnerController.getDeliveryPartnerByEmail(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deliveryPartner.getDeliveryPartnerId(), response.getBody());
    }
}