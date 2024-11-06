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
import sg.edu.nus.iss.profile_service.dto.CustomerDTO;
import sg.edu.nus.iss.profile_service.factory.ProfileServiceFactory;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.Profile;
import sg.edu.nus.iss.profile_service.model.Rewards;

import java.math.BigDecimal;
import java.util.*;

public class CustomerControllerTest {

    @Mock
    private ProfileServiceFactory profileServiceFactory;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test getAllCustomers without pagination
    @Test
    public void testGetAllCustomersWithoutPagination() {
        List<Profile> customers = List.of(new Customer());
        when(profileServiceFactory.getProfilesByType("customer")).thenReturn(customers);

        ResponseEntity<?> response = customerController.getAllCustomers(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customers, response.getBody());
        verify(profileServiceFactory, times(1)).getProfilesByType("customer");
    }

    // Test getCustomerById - customer found
    @Test
    public void testGetCustomerById_Success() {
        Customer customer = new Customer();
        when(profileServiceFactory.getProfileById(eq("customer"), any(UUID.class))).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = customerController.getCustomer(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customer, response.getBody());
    }

    // Test getCustomerById - customer not found
    @Test
    public void testGetCustomerById_NotFound() {
        when(profileServiceFactory.getProfileById(eq("customer"), any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<?> response = customerController.getCustomer(UUID.randomUUID());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Test updateCustomer - success
    @Test
    public void testUpdateCustomer_Success() {
        UUID customerId = UUID.randomUUID();
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCustomerId(customerId);
        customerDTO.setEmailAddress("test@example.com");
        customerDTO.setName("test");

        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setEmailAddress("test@example.com");
        customer.setName("test");
        when(profileServiceFactory.getProfileById(eq("customer"), eq(customerDTO.getCustomerId()))).thenReturn(Optional.of(customer));
        when(mapper.convertValue(any(CustomerDTO.class), eq(Customer.class))).thenReturn(customer);

        ResponseEntity<?> response = customerController.updateCustomer(customerDTO.getCustomerId(), customerDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Customer updated successfully", response.getBody());
        verify(profileServiceFactory, times(1)).updateProfile(customer);
    }

    // Test updateCustomer - email mismatch
    @Test
    public void testUpdateCustomer_EmailMismatch() {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCustomerId(UUID.randomUUID());
        customerDTO.setEmailAddress("new@example.com");
        customerDTO.setName("new");

        Customer customer = new Customer();
        customer.setName("new");
        customer.setEmailAddress("old@example.com");
        customer.setCustomerId(customerDTO.getCustomerId());
        when(profileServiceFactory.getProfileById(eq("customer"), eq(customerDTO.getCustomerId()))).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = customerController.updateCustomer(customerDTO.getCustomerId(), customerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email shouldn't be changed", response.getBody());
    }

    // Test deleteCustomer - success
    @Test
    public void testDeleteCustomer_Success() {
        UUID customerId = UUID.randomUUID();

        ResponseEntity<String> response = customerController.deleteCustomer(customerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete: successful", response.getBody());
        verify(profileServiceFactory, times(1)).deleteProfile(customerId);
    }

    // Test registerCustomer - success
    @Test
    public void testRegisterCustomer_Success() {
        Customer customer = new Customer();
        customer.setEmailAddress("test@example.com");

        when(profileServiceFactory.getProfileByEmailAddress(customer.getEmailAddress(), "customer")).thenReturn(Optional.empty());

        ResponseEntity<String> response = customerController.registerCustomer(customer);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Created customer", response.getBody());
        verify(profileServiceFactory, times(1)).createProfile(customer);
    }

    // Test registerCustomer - email already registered
    @Test
    public void testRegisterCustomer_EmailAlreadyRegistered() {
        Customer customer = new Customer();
        customer.setEmailAddress("test@example.com");

        when(profileServiceFactory.getProfileByEmailAddress(customer.getEmailAddress(), "customer")).thenReturn(Optional.of(new Customer()));

        ResponseEntity<String> response = customerController.registerCustomer(customer);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }

    // Test validation error handling
    @Test
    public void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("customer", "email", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        Map<String, String> errors = customerController.handleValidationExceptions(ex);

        assertEquals(1, errors.size());
        assertEquals("must not be null", errors.get("email"));
    }


    // Test getCustomerByEmail - customer found
    @Test
    public void testGetCustomerByEmail_Success() {
        Customer customer = new Customer();
        when(profileServiceFactory.getProfileByEmailAddress("testcustomer@gmail.com", "customer")).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = customerController.getCustomerByEmail("testcustomer@gmail.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customer.getCustomerId(), response.getBody());
    }


    // add a test for patchRewardPoints and retrieveRewardDetails

    // Test patchRewardPoints - success
    @Test
    public void testPatchRewardPoints_Success() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setRewardPoints(BigDecimal.valueOf(100));
        when(profileServiceFactory.getProfileById("customer", customerId)).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = customerController.patchRewardPoints(customerId, BigDecimal.valueOf(100));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Reward points updated successfully", response.getBody());
        assertEquals(BigDecimal.valueOf(100.0), customer.getRewardPoints());
        verify(profileServiceFactory, times(1)).updateProfile(customer);
    }

    // Test patchRewardPoints - customer not found
    @Test
    public void testPatchRewardPoints_CustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        when(profileServiceFactory.getProfileById("customer", customerId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = customerController.patchRewardPoints(customerId, BigDecimal.valueOf(100));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Customer Not found", response.getBody());
    }

    @Test
    public void testRetrieveRewardDetails_Success() {
        UUID customerId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setRewardPoints(BigDecimal.valueOf(100));
        when(profileServiceFactory.getProfileById("customer", customerId)).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = customerController.retrieveRewardDetails(customerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, ((Rewards) response.getBody()).getRewardAmount().compareTo(BigDecimal.valueOf(1.0)));
        assertEquals(BigDecimal.valueOf(100), ((Rewards) response.getBody()).getRewardPoints());
    }
}