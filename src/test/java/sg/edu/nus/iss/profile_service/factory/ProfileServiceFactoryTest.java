package sg.edu.nus.iss.profile_service.factory;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.hibernate.query.sqm.mutation.internal.cte.CteInsertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sg.edu.nus.iss.profile_service.model.*;
import sg.edu.nus.iss.profile_service.repository.DeliveryPartnerRepository;
import sg.edu.nus.iss.profile_service.repository.MerchantRepository;
import sg.edu.nus.iss.profile_service.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProfileServiceFactoryTest {

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ExternalLocationService externalLocationService;

    @InjectMocks
    private ProfileServiceFactory profileServiceFactory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetProfilesWithPagination_Merchant_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Merchant> merchants = List.of(new Merchant());
        Page<Merchant> merchantPage = new PageImpl<>(merchants, pageable, merchants.size());

        when(merchantRepository.findAllByDeletedFalse(pageable)).thenReturn(merchantPage);

        Page<Profile> result = profileServiceFactory.getProfilesWithPagination("merchant", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(merchantRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    public void testGetProfilesWithPagination_Partner_Success(){
        Pageable pageable = PageRequest.of(0, 10);
        List<DeliveryPartner> deliveryPartners = List.of(new DeliveryPartner());
        Page<DeliveryPartner> deliveryPartnerPage = new PageImpl<>(deliveryPartners, pageable, deliveryPartners.size());

        when(deliveryPartnerRepository.findAllByDeletedFalse(pageable)).thenReturn(deliveryPartnerPage);

        Page<Profile> result = profileServiceFactory.getProfilesWithPagination("deliveryPartner", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(deliveryPartnerRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    public void testGetProfilesWithPagination_Customer_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = List.of(new Customer());
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.findAllByDeletedFalse(pageable)).thenReturn(customerPage);

        Page<Profile> result = profileServiceFactory.getProfilesWithPagination("customer", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    public void testGetProfilesWithPagination_InvalidType_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.getProfilesWithPagination("invalidType", pageable);
        });

        assertEquals("Invalid profile type", exception.getMessage());
    }

    @Test
    public void testCreateMerchantProfile_Success() {
        Merchant merchant = new Merchant();
        merchant.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(merchant.getPincode())).thenReturn(latLng);

        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

        Profile result = profileServiceFactory.createProfile(merchant);

        assertNotNull(result);
        assertEquals(merchant, result);
        verify(merchantRepository, times(1)).save(merchant);
        verify(externalLocationService, times(1)).getCoordinates("228714");
    }

    @Test
    public void testCreateDeliveryProfile_Success(){
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(deliveryPartner.getPincode())).thenReturn(latLng);

        when(deliveryPartnerRepository.save(any(DeliveryPartner.class))).thenReturn(deliveryPartner);

        Profile result = profileServiceFactory.createProfile(deliveryPartner);

        assertNotNull(result);
        assertEquals(deliveryPartner, result);
        verify(deliveryPartnerRepository, times(1)).save(deliveryPartner);
        verify(externalLocationService, times(1)).getCoordinates("228714");
    }

    @Test
    public void testCreateCustomerProfile_Success() {
        Customer customer = new Customer();
        customer.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(customer.getPincode())).thenReturn(latLng);

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Profile result = profileServiceFactory.createProfile(customer);

        assertNotNull(result);
        assertEquals(customer, result);
        verify(customerRepository, times(1)).save(customer);
        verify(externalLocationService, times(1)).getCoordinates("228714");
    }

    // Test for invalid profile creation
    @Test
    public void testCreateInvalidProfile_ThrowsException() {
        Profile invalidProfile = mock(Profile.class); // Create a mock profile

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.createProfile(invalidProfile);
        });

        assertEquals("Invalid profile type", exception.getMessage());
    }

    // Test for updateProfile
    @Test
    public void testUpdateMerchantProfile_Success() {
        Merchant merchant = new Merchant();
        UUID merchantId = UUID.randomUUID();
        merchant.setMerchantId(merchantId);
        merchant.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(merchant.getPincode())).thenReturn(latLng);

        profileServiceFactory.updateProfile(merchant);

        verify(merchantRepository, times(1)).save(merchant);
        verify(externalLocationService, times(1)).getCoordinates("228714");
    }

    @Test
    public void testUpdateDeliveryProfile_Success(){
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        UUID deliveryPartnerId = UUID.randomUUID();
        deliveryPartner.setDeliveryPartnerId(deliveryPartnerId);
        deliveryPartner.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(deliveryPartner.getPincode())).thenReturn(latLng);

        profileServiceFactory.updateProfile(deliveryPartner);

        verify(deliveryPartnerRepository, times(1)).save(deliveryPartner);
        verify(externalLocationService, times(1)).getCoordinates("228714");
    }

    @Test
    public void testUpdateCustomerProfile_Success() {
        Customer customer = new Customer();
        UUID customerId = UUID.randomUUID();
        customer.setCustomerId(customerId);
        customer.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(customer.getPincode())).thenReturn(latLng);

        profileServiceFactory.updateProfile(customer);

        verify(customerRepository, times(1)).save(customer);
        verify(externalLocationService, times(1)).getCoordinates("228714");
    }

    @Test
    public void testUpdateInvalidProfile_ThrowsException() {
        Profile invalidProfile = mock(Profile.class); // Simulate invalid profile type

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.updateProfile(invalidProfile);
        });

        assertEquals("Invalid profile type", exception.getMessage());
    }

    // Test for deleteProfile
    @Test
    public void testDeleteMerchantProfile_Success() {
        Merchant merchant = new Merchant();
        UUID merchantId = UUID.randomUUID();
        merchant.setMerchantId(merchantId);

        when(merchantRepository.findByMerchantIdAndDeletedFalse(merchantId)).thenReturn(Optional.of(merchant));

        profileServiceFactory.deleteProfile(merchantId);

        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    public void testDeletePartnerProfile_Success(){
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        UUID deliveryPartnerId = UUID.randomUUID();
        deliveryPartner.setDeliveryPartnerId(deliveryPartnerId);

        when(deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(deliveryPartnerId)).thenReturn(Optional.of(deliveryPartner));

        profileServiceFactory.deleteProfile(deliveryPartnerId);

        verify(deliveryPartnerRepository, times(1)).save(deliveryPartner);
    }

    @Test
    public void testDeleteCustomerProfile_Success() {
        Customer customer = new Customer();
        UUID customerId = UUID.randomUUID();
        customer.setCustomerId(customerId);

        when(customerRepository.findByCustomerIdAndDeletedFalse(customerId)).thenReturn(Optional.of(customer));

        profileServiceFactory.deleteProfile(customerId);

        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void testDeleteInvalidProfile_ThrowsException() {
        UUID invalidId = UUID.randomUUID();

        when(merchantRepository.findByMerchantIdAndDeletedFalse(invalidId)).thenReturn(Optional.empty());
        when(deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(invalidId)).thenReturn(Optional.empty());
        when(customerRepository.findByCustomerIdAndDeletedFalse(invalidId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.deleteProfile(invalidId);
        });

        assertEquals("Invalid profile id", exception.getMessage());
    }

    // Test for getProfileById
    @Test
    public void testGetMerchantProfileById_Success() {
        Merchant merchant = new Merchant();
        UUID merchantId = UUID.randomUUID();
        merchant.setMerchantId(merchantId);

        when(merchantRepository.findByMerchantIdAndDeletedFalse(merchantId)).thenReturn(Optional.of(merchant));

        Optional<Profile> result = profileServiceFactory.getProfileById("merchant", merchantId);

        assertTrue(result.isPresent());
        assertEquals(merchant, result.get());
    }

    @Test
    public void testGetDeliveryPartnerProfileById_Success() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        UUID deliveryPartnerId = UUID.randomUUID();
        deliveryPartner.setDeliveryPartnerId(deliveryPartnerId);

        when(deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(deliveryPartnerId)).thenReturn(Optional.of(deliveryPartner));

        Optional<Profile> result = profileServiceFactory.getProfileById("deliveryPartner", deliveryPartnerId);

        assertTrue(result.isPresent());
        assertEquals(deliveryPartner, result.get());
    }

    @Test
    public void testGetCustomerProfileById_Success() {
        Customer customer = new Customer();
        UUID customerId = UUID.randomUUID();
        customer.setCustomerId(customerId);

        when(customerRepository.findByCustomerIdAndDeletedFalse(customerId)).thenReturn(Optional.of(customer));

        Optional<Profile> result = profileServiceFactory.getProfileById("customer", customerId);

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
    }

    @Test
    public void testGetProfileByInvalidType_ThrowsException() {
        UUID profileId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.getProfileById("invalidType", profileId);
        });

        assertEquals("Invalid profile type", exception.getMessage());
    }

    // Test for setMerchantCoordinates
    @Test
    public void testSetMerchantCoordinates_Success() {
        Merchant merchant = new Merchant();
        merchant.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(merchant.getPincode())).thenReturn(latLng);

        profileServiceFactory.setMerchantCoordinates(merchant);

        assertEquals(1.3521, merchant.getLatitude());
        assertEquals(103.8198, merchant.getLongitude());
        verify(externalLocationService, times(1)).getCoordinates(merchant.getPincode());
    }

    @Test
    public void testSetDeliveryPartnerCoordinates_Success() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(deliveryPartner.getPincode())).thenReturn(latLng);

        profileServiceFactory.setDeliveryPartnerCoordinates(deliveryPartner);

        assertEquals(1.3521, deliveryPartner.getLatitude());
        assertEquals(103.8198, deliveryPartner.getLongitude());
        verify(externalLocationService, times(1)).getCoordinates(deliveryPartner.getPincode());
    }

    @Test
    public void testSetMerchantCoordinates_InvalidPincode_ThrowsException() {
        Merchant merchant = new Merchant();
        merchant.setPincode("invalidPincode");

        when(externalLocationService.getCoordinates(merchant.getPincode())).thenThrow(new IllegalArgumentException("Invalid pincode"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.setMerchantCoordinates(merchant);
        });

        assertEquals("Invalid pincode: invalidPincode", exception.getMessage());
    }

    @Test
    public void testSetDeliveryPartnerCoordinates_InvalidPincode_ThrowsException() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setPincode("invalidPincode");

        when(externalLocationService.getCoordinates(deliveryPartner.getPincode())).thenThrow(new IllegalArgumentException("Invalid pincode"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.setDeliveryPartnerCoordinates(deliveryPartner);
        });

        assertEquals("Invalid pincode: invalidPincode", exception.getMessage());
    }

    @Test
    public void testSetCustomerCoordinates_Success() {
        Customer customer = new Customer();
        customer.setPincode("228714");

        LatLng latLng = new LatLng(1.3521, 103.8198);
        when(externalLocationService.getCoordinates(customer.getPincode())).thenReturn(latLng);

        profileServiceFactory.setCustomerCoordinates(customer);

        assertEquals(1.3521, customer.getLatitude());
        assertEquals(103.8198, customer.getLongitude());
        verify(externalLocationService, times(1)).getCoordinates(customer.getPincode());
    }

    @Test
    public void testSetCustomerCoordinates_InvalidPincode_ThrowsException() {
        Customer customer = new Customer();
        customer.setPincode("invalidPincode");

        when(externalLocationService.getCoordinates(customer.getPincode())).thenThrow(new IllegalArgumentException("Invalid pincode"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.setCustomerCoordinates(customer);
        });

        assertEquals("Invalid pincode: invalidPincode", exception.getMessage());
    }

    @Test
    public void testGetProfilesByType_Merchant_Success() {
        List<Merchant> merchants = List.of(new Merchant());
        when(merchantRepository.findAllByDeletedFalse()).thenReturn(merchants);

        List<Profile> result = profileServiceFactory.getProfilesByType("merchant");

        assertEquals(1, result.size());
        verify(merchantRepository, times(1)).findAllByDeletedFalse();
    }

    @Test void testGetProfilesByType_DeliveryPartner_Success() {
        List<DeliveryPartner> deliveryPartners = List.of(new DeliveryPartner());
        when(deliveryPartnerRepository.findAllByDeletedFalse()).thenReturn(deliveryPartners);

        List<Profile> result = profileServiceFactory.getProfilesByType("deliveryPartner");

        assertEquals(1, result.size());
        verify(deliveryPartnerRepository, times(1)).findAllByDeletedFalse();
    }

    @Test
    public void testGetProfilesByType_Customer_Success() {
        List<Customer> customers = List.of(new Customer());
        when(customerRepository.findAllByDeletedFalse()).thenReturn(customers);

        List<Profile> result = profileServiceFactory.getProfilesByType("customer");

        assertEquals(1, result.size());
        verify(customerRepository, times(1)).findAllByDeletedFalse();
    }

    @Test
    public void testGetProfilesByType_InvalidType_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.getProfilesByType("invalidType");
        });

        assertEquals("Invalid profile type", exception.getMessage());
    }

    @Test
    public void testGetProfileByEmailAddress_Merchant_Success() {
        Merchant merchant = new Merchant();
        when(merchantRepository.findByEmailAddressAndDeletedFalse("test@merchant.com")).thenReturn(Optional.of(merchant));

        Optional<Profile> result = profileServiceFactory.getProfileByEmailAddress("test@merchant.com", "merchant");

        assertTrue(result.isPresent());
        assertEquals(merchant, result.get());
        verify(merchantRepository, times(1)).findByEmailAddressAndDeletedFalse("test@merchant.com");
    }
    @Test
    public void testGetProfileByEmailAddress_DeliveryPartner_Success() {
        DeliveryPartner deliveryPartner = new DeliveryPartner();
        when(deliveryPartnerRepository.findByEmailAddressAndDeletedFalse("test@partner.com")).thenReturn(Optional.of(deliveryPartner));
        Optional<Profile> result = profileServiceFactory.getProfileByEmailAddress("test@partner.com", "deliveryPartner");

        assertTrue(result.isPresent());
        assertEquals(deliveryPartner, result.get());
        verify(deliveryPartnerRepository, times(1)).findByEmailAddressAndDeletedFalse("test@partner.com");
    }

    @Test
    public void testGetProfileByEmailAddress_Customer_Success() {
        Customer customer = new Customer();
        when(customerRepository.findByEmailAddressAndDeletedFalse("test@customer.com")).thenReturn(Optional.of(customer));

        Optional<Profile> result = profileServiceFactory.getProfileByEmailAddress("test@customer.com", "customer");

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
        verify(customerRepository, times(1)).findByEmailAddressAndDeletedFalse("test@customer.com");
    }

    @Test
    public void testGetProfileByEmailAddress_InvalidType_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            profileServiceFactory.getProfileByEmailAddress("test@invalid.com", "invalidType");
        });

        assertEquals("Invalid profile type", exception.getMessage());
    }




}
