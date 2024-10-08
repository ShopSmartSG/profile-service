package sg.edu.nus.iss.profile_service.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ExternalLocationService externalLocationService;


    @Override
    public Profile createProfile(Profile profile) {
        if (profile instanceof Merchant) {
            setMerchantCoordinates((Merchant) profile);
            return merchantRepository.save((Merchant) profile);
        } else if (profile instanceof Customer) {
            setCustomerCoordinates((Customer) profile);
            return customerRepository.save((Customer) profile);
        }
        throw new IllegalArgumentException("Invalid profile type");
    }

    @Override
    public void updateProfile(Profile profile) {
        if (profile instanceof Merchant) {
            setMerchantCoordinates((Merchant) profile);
            merchantRepository.save((Merchant) profile);
            return;
        } else if (profile instanceof Customer) {
            setCustomerCoordinates((Customer) profile);
            customerRepository.save((Customer) profile);
            return;
        }
        throw new IllegalArgumentException("Invalid profile type");
    }

    @Override
    public void deleteProfile(UUID id) {
        Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
        if (merchant.isPresent()) {
            Merchant m = merchant.get();
            m.setDeleted(true);
            merchantRepository.save(m);
            return;
        }

        Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            c.setDeleted(true);
            customerRepository.save(c);
            return;
        }

        throw new IllegalArgumentException("Invalid profile id");
    }


    @Override
    public Optional<Profile> getProfileById(String type, UUID id) {

        if ("merchant".equalsIgnoreCase(type)) {
            Optional<Merchant> merchant = merchantRepository.findByMerchantIdAndDeletedFalse(id);
            return Optional.ofNullable(merchant.orElse(null));
        }else if ("customer".equalsIgnoreCase(type)) {
            Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
            return Optional.ofNullable(customer.orElse(null));
        }else {
            throw new IllegalArgumentException("Invalid profile type");
        }
    }




    public List<Profile> getProfilesByType(String type) {
        if ("merchant".equalsIgnoreCase(type)) {
            return new ArrayList<>(merchantRepository.findAllByDeletedFalse());
        } else if ("customer".equalsIgnoreCase(type)) {
            return new ArrayList<>(customerRepository.findAllByDeletedFalse());
        } else {
            throw new IllegalArgumentException("Invalid profile type");
        }
    }


    @Override
    public Optional<Profile> getProfileByEmailAddress(String email, String type) {

        if ("merchant".equalsIgnoreCase(type)) {
            Optional<Merchant> merchant = merchantRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(merchant.orElse(null));
        }else if ("customer".equalsIgnoreCase(type)) {
            Optional<Customer> customer = customerRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.ofNullable(customer.orElse(null));
        }else {
            throw new IllegalArgumentException("Invalid profile type");
        }
    }

    private void setMerchantCoordinates(Merchant merchant) {
        try {
            LatLng coordinates = externalLocationService.getCoordinates(merchant.getPincode());
            merchant.setLatitude(coordinates.getLat());
            merchant.setLongitude(coordinates.getLng());
        }catch (RuntimeException e){
            throw new RuntimeException("Error fetching coordinates from external service", e);
        }

    }

    private void setCustomerCoordinates(Customer customer) {
        LatLng coordinates = externalLocationService.getCoordinates(customer.getPincode());
        customer.setLatitude(coordinates.getLat());
        customer.setLongitude(coordinates.getLng());
    }

}