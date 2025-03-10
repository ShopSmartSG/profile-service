package sg.edu.nus.iss.profile_service.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.profile_service.model.*;
import sg.edu.nus.iss.profile_service.repository.CustomerRepository;
import sg.edu.nus.iss.profile_service.repository.DeliveryPartnerRepository;
import sg.edu.nus.iss.profile_service.repository.MerchantRepository;
import sg.edu.nus.iss.profile_service.service.ProfileService;
import sg.edu.nus.iss.profile_service.util.LogMasker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileServiceFactory implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceFactory.class);

    @Autowired
    LogMasker logMasker;
    private final MerchantRepository merchantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final CustomerRepository customerRepository;
    private final ExternalLocationService externalLocationService;

    private static final String MERCHANT = "merchant";
    private static final String DELIVERY_PARTNER = "deliveryPartner";
    private static final String CUSTOMER = "customer";
    private static final String INVALID_PROFILE_TYPE = "Invalid profile type";

    @Autowired
    public ProfileServiceFactory(MerchantRepository merchantRepository, CustomerRepository customerRepository, DeliveryPartnerRepository deliveryPartnerRepository, ExternalLocationService externalLocationService) {
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.externalLocationService = externalLocationService;
    }

    @Override
    public Profile createProfile(Profile profile) {
        if (profile instanceof Merchant ) {
            Merchant merchant = (Merchant) profile;
            log.info("{\"message\": \"Creating merchant profile : {}\"}", logMasker.maskEntity(merchant));
            setMerchantCoordinates(merchant);
            return merchantRepository.save(merchant);
        } else if (profile instanceof Customer) {
            Customer customer = (Customer) profile;
            log.info("{\"message\": \"Creating customer profile : {}\"}", logMasker.maskEntity(customer));
            setCustomerCoordinates(customer);
            return customerRepository.save(customer);
        }else if (profile instanceof DeliveryPartner ) {
            DeliveryPartner deliveryPartner = (DeliveryPartner) profile;
            log.info("{\"message\": \"Creating delivery partner profile : {}\"}", logMasker.maskEntity(deliveryPartner));
            setDeliveryPartnerCoordinates(deliveryPartner);
            return deliveryPartnerRepository.save(deliveryPartner);
        }
        throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
    }

    @Override
    public void updateProfile(Profile profile) {
        if (profile instanceof Merchant ) {
            Merchant merchant = (Merchant) profile;
            log.info("{\"message\": \"Updating merchant profile : {}\"}", logMasker.maskEntity(merchant));
            setProfileCoordinates(merchant);
            merchantRepository.save(merchant);
            return;
        } else if (profile instanceof Customer) {
            Customer customer = (Customer) profile;
            log.info("{\"message\": \"Updating customer profile : {}\"}", logMasker.maskEntity(customer));
            setCustomerCoordinates(customer);
            customerRepository.save(customer);
            return;
        }else if (profile instanceof DeliveryPartner ) {
            DeliveryPartner deliveryPartner = (DeliveryPartner) profile;
            log.info("{\"message\": \"Updating delivery partner profile : {}\"}", logMasker.maskEntity(deliveryPartner));
            setDeliveryPartnerCoordinates(deliveryPartner);
            deliveryPartnerRepository.save(deliveryPartner);
            return;
        }
        throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
    }

    @Override
    public void deleteProfile(UUID id) {
        Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
        if (merchant.isPresent()) {
            log.info("{\"message\": \"Deleting merchant profile with ID: {}\"}", id);
            Merchant m = merchant.get();
            m.setDeleted(true);
            merchantRepository.save(m);
            return;
        }

        Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
        if (customer.isPresent()) {
            log.info("{\"message\": \"Deleting customer profile with ID: {}\"}", id);
            Customer c = customer.get();
            c.setDeleted(true);
            customerRepository.save(c);
            return;
        }

        Optional<DeliveryPartner> deliveryPartner = deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(id);
        if (deliveryPartner.isPresent()) {
            log.info("{\"message\": \"Deleting delivery partner profile with ID: {}\"}", id);
            DeliveryPartner dp = deliveryPartner.get();
            dp.setDeleted(true);
            deliveryPartnerRepository.save(dp);
            return;
        }

        throw new IllegalArgumentException("Invalid profile id");
    }

    @Override
    public void blacklistProfile(UUID id) {
        Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
        if (merchant.isPresent()) {
            Merchant m = merchant.get();
            m.setBlacklisted(true);
            merchantRepository.save(m);
            return;
        }else if (deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(id).isPresent()) {
            DeliveryPartner dp = deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(id).get();
            dp.setBlacklisted(true);
            deliveryPartnerRepository.save(dp);
            return;
        }

        throw new IllegalArgumentException("Invalid profile id");
    }

    @Override
    public void unblacklistProfile(UUID id) {
        Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
        if (merchant.isPresent()) {
            Merchant m = merchant.get();
            m.setBlacklisted(false);
            merchantRepository.save(m);
            return;
        }else if (deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(id).isPresent()) {
            DeliveryPartner dp = deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(id).get();
            dp.setBlacklisted(false);
            deliveryPartnerRepository.save(dp);
            return;
        }

        throw new IllegalArgumentException("Invalid profile id");
    }

    @Override
    public void setProfileCoordinates(Profile profile) {
        if (profile instanceof Merchant ) {
            Merchant merchant = (Merchant) profile;
            setMerchantCoordinates(merchant);
            return;
        } else if (profile instanceof Customer) {
            Customer customer = (Customer) profile;
            setCustomerCoordinates(customer);
            return;
        }else if (profile instanceof DeliveryPartner ) {
            DeliveryPartner deliveryPartner = (DeliveryPartner) profile;
            setDeliveryPartnerCoordinates(deliveryPartner);
            return;
        }
        throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
    }


    @Override
    public Optional<Profile> getProfileById(String type, UUID id) {

        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching merchant with ID: " + id + "\"}");
            Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
            return Optional.ofNullable(merchant.orElse(null));
        }else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching customer with ID: {}\"}", id);
            Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
            return Optional.ofNullable(customer.orElse(null));
        }else if (DELIVERY_PARTNER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching delivery partner with ID: {}\"}", id);
            Optional<DeliveryPartner> deliveryPartner = deliveryPartnerRepository.findByDeliveryPartnerIdAndDeletedFalse(id);
            return Optional.ofNullable(deliveryPartner.orElse(null));
        }else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }




    public List<Profile> getProfilesByType(String type) {
        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching all merchants\"}");
            return new ArrayList<>(merchantRepository.findAllByDeletedFalse());
        } else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching all customers\"}");
            return new ArrayList<>(customerRepository.findAllByDeletedFalse());
        } else if (DELIVERY_PARTNER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching all delivery partners\"}");
            return new ArrayList<>(deliveryPartnerRepository.findAllByDeletedFalse());
        }else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }

    @Override
    public Page<Profile> getProfilesWithPagination(String type, Pageable pageable) {
        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching merchants with pagination attributes: page {} and size {}\"}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Merchant> merchantPage = merchantRepository.findAllByDeletedFalse(pageable);
            return merchantPage.map(merchant -> (Profile) merchant);
        } else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching customers with pagination attributes: page {} and size {}\"}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Customer> customerPage = customerRepository.findAllByDeletedFalse(pageable);
            return customerPage.map(customer -> (Profile) customer);
        } else if (DELIVERY_PARTNER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching delivery partners with pagination attributes: page {} and size {}\"}", pageable.getPageNumber(), pageable.getPageSize());
            Page<DeliveryPartner> deliveryPartnerPage = deliveryPartnerRepository.findAllByDeletedFalse(pageable);
            return deliveryPartnerPage.map(deliveryPartner -> (Profile) deliveryPartner);
        }else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }


    @Override
    public Optional<Profile> getProfileByEmailAddress(String email, String type) {

        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching merchant with email: {}\"}", logMasker.maskEmail(email));
            Optional<Merchant> merchant = merchantRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(merchant.orElse(null));
        }else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching customer with email: {}\"}", logMasker.maskEmail(email));
            Optional<Customer> customer = customerRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(customer.orElse(null));
        }else if (DELIVERY_PARTNER.equalsIgnoreCase(type)) {
            log.info("{\"message\": \"Fetching delivery partner with email: {}\"}", logMasker.maskEmail(email));
            Optional<DeliveryPartner> deliveryPartner = deliveryPartnerRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(deliveryPartner.orElse(null));
        }else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }

    private void setMerchantCoordinates(Merchant merchant) {
        try {
            LatLng coordinates = externalLocationService.getCoordinates(merchant.getPincode());

            if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates not found for pincode: " + merchant.getPincode());
            }

            log.info("{\"message\": \"Setting coordinates for merchant with pincode: {} - {}\"}", logMasker.maskEmail(merchant.getPincode()), coordinates);
            merchant.setLatitude(coordinates.getLat());
            merchant.setLongitude(coordinates.getLng());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pincode: " + merchant.getPincode(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching coordinates from external service for pincode: " + merchant.getPincode(), e);
        }
    }



    private void setDeliveryPartnerCoordinates(DeliveryPartner deliveryPartner) {
        try {
            LatLng coordinates = externalLocationService.getCoordinates(deliveryPartner.getPincode());

            if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates not found for pincode: " + deliveryPartner.getPincode());
            }

            log.info("{\"message\": \"Setting coordinates for delivery partner with pincode: {} - {}\"}", deliveryPartner.getPincode(), coordinates);
            deliveryPartner.setLatitude(coordinates.getLat());
            deliveryPartner.setLongitude(coordinates.getLng());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pincode: " + deliveryPartner.getPincode(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching coordinates from external service for pincode: " + deliveryPartner.getPincode(), e);
        }
    }

    private void setCustomerCoordinates(Customer customer) {
        try {
            LatLng coordinates = externalLocationService.getCoordinates(customer.getPincode());

            if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates not found for pincode: " + customer.getPincode());
            }


            log.info("{\"message\": \"Setting coordinates for customer with pincode: {} - {}\"}", customer.getPincode(), coordinates);
            customer.setLatitude(coordinates.getLat());
            customer.setLongitude(coordinates.getLng());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pincode: " + customer.getPincode(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching coordinates from external service for pincode: " + customer.getPincode(), e);
        }
    }


}