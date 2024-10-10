package sg.edu.nus.iss.profile_service.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.LatLng;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.model.Profile;
import sg.edu.nus.iss.profile_service.repository.CustomerRepository;
import sg.edu.nus.iss.profile_service.repository.MerchantRepository;
import sg.edu.nus.iss.profile_service.service.ProfileService;

@Service
public class ProfileServiceFactory implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceFactory.class);

    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;
    private final ExternalLocationService externalLocationService;

    private static final String MERCHANT = "merchant";
    private static final String CUSTOMER = "customer";
    private static final String INVALID_PROFILE_TYPE = "Invalid profile type";

    @Autowired
    public ProfileServiceFactory(MerchantRepository merchantRepository, CustomerRepository customerRepository, ExternalLocationService externalLocationService) {
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
        this.externalLocationService = externalLocationService;
    }

    @Override
    public Profile createProfile(Profile profile) {
        if (profile instanceof Merchant merchant) {
            log.info("Creating merchant profile : {} ", merchant);
            setMerchantCoordinates(merchant);
            return merchantRepository.save(merchant);
        } else if (profile instanceof Customer customer) {
            log.info("Creating customer profile : {} ", customer);
            setCustomerCoordinates(customer);
            return customerRepository.save(customer);
        }
        throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
    }

    @Override
    public void updateProfile(Profile profile) {
        if (profile instanceof Merchant merchant) {
            log.info("Updating merchant profile : {} ", merchant);
            setMerchantCoordinates(merchant);
            merchantRepository.save(merchant);
            return;
        } else if (profile instanceof Customer customer) {
            log.info("Updating customer profile : {} ", customer);
            setCustomerCoordinates(customer);
            customerRepository.save(customer);
            return;
        }
        throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
    }

    @Override
    public void deleteProfile(UUID id) {
        Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
        if (merchant.isPresent()) {
            log.info("Deleting merchant profile with ID: {}", id);
            Merchant m = merchant.get();
            m.setDeleted(true);
            merchantRepository.save(m);
            return;
        }

        Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
        if (customer.isPresent()) {
            log.info("Deleting customer profile with ID: {}", id);
            Customer c = customer.get();
            c.setDeleted(true);
            customerRepository.save(c);
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
        }

        throw new IllegalArgumentException("Invalid profile id");
    }


    @Override
    public Optional<Profile> getProfileById(String type, UUID id) {

        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("Fetching merchant with ID: {}", id);
            Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
            return Optional.ofNullable(merchant.orElse(null));
        }else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("Fetching customer with ID: {}", id);
            Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
            return Optional.ofNullable(customer.orElse(null));
        }else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }




    public List<Profile> getProfilesByType(String type) {
        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("Fetching all merchants via factory");
            return new ArrayList<>(merchantRepository.findAllByDeletedFalse());
        } else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("Fetching all customers via factory");
            return new ArrayList<>(customerRepository.findAllByDeletedFalse());
        } else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }

    @Override
    public Page<Profile> getProfilesWithPagination(String type, Pageable pageable) {
        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("Fetching merchants with pagination attributes: page {} and size {}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Merchant> merchantPage = merchantRepository.findAllByDeletedFalse(pageable);
            return merchantPage.map(merchant -> (Profile) merchant);
        } else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("Fetching customers with pagination attributes: page {} and size {}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Customer> customerPage = customerRepository.findAllByDeletedFalse(pageable);
            return customerPage.map(customer -> (Profile) customer);
        } else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }


    @Override
    public Optional<Profile> getProfileByEmailAddress(String email, String type) {

        if (MERCHANT.equalsIgnoreCase(type)) {
            log.info("Fetching merchant with email: {}", email);
            Optional<Merchant> merchant = merchantRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(merchant.orElse(null));
        }else if (CUSTOMER.equalsIgnoreCase(type)) {
            log.info("Fetching customer with email: {}", email);
            Optional<Customer> customer = customerRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(customer.orElse(null));
        }else {
            throw new IllegalArgumentException(INVALID_PROFILE_TYPE);
        }
    }

    public void setMerchantCoordinates(Merchant merchant) {
        try {
            LatLng coordinates = externalLocationService.getCoordinates(merchant.getPincode());

            if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates not found for pincode: " + merchant.getPincode());
            }

            log.info("Setting coordinates for merchant with pincode: {} - {}", merchant.getPincode(), coordinates);
            merchant.setLatitude(coordinates.getLat());
            merchant.setLongitude(coordinates.getLng());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pincode: " + merchant.getPincode(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching coordinates from external service for pincode: " + merchant.getPincode(), e);
        }
    }

    public void setCustomerCoordinates(Customer customer) {
        try {
            LatLng coordinates = externalLocationService.getCoordinates(customer.getPincode());

            if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates not found for pincode: " + customer.getPincode());
            }


            log.info("Setting coordinates for customer with pincode: {} - {}", customer.getPincode(), coordinates);
            customer.setLatitude(coordinates.getLat());
            customer.setLongitude(coordinates.getLng());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pincode: " + customer.getPincode(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching coordinates from external service for pincode: " + customer.getPincode(), e);
        }
    }


}