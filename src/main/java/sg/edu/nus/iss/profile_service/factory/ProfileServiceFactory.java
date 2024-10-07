package sg.edu.nus.iss.profile_service.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.profile_service.model.Customer;
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

    public ProfileServiceFactory(MerchantRepository merchantRepository, CustomerRepository customerRepository) {
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Profile createProfile(Profile profile) {
        if (profile instanceof Merchant) {
            return merchantRepository.save((Merchant) profile);
        } else if (profile instanceof Customer) {
            return customerRepository.save((Customer) profile);
        }
        throw new IllegalArgumentException("Invalid profile type");
    }

    @Override
    public void updateProfile(Profile profile) {
        if (profile instanceof Merchant) {
            merchantRepository.save((Merchant) profile);
            return;
        } else if (profile instanceof Customer) {
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
            return Optional.of(merchant.get());
        }else if ("customer".equalsIgnoreCase(type)) {
            Optional<Customer> customer = customerRepository.findByCustomerIdAndDeletedFalse(id);
            return Optional.of(customer.get());
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
            return Optional.of(merchant.get());
        }else if ("customer".equalsIgnoreCase(type)) {
            Optional<Customer> customer = customerRepository.findByEmailAddressAndDeletedFalse(email);
            return Optional.of(customer.get());
        }else {
            throw new IllegalArgumentException("Invalid profile type");
        }
    }

}