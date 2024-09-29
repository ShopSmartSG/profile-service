package sg.edu.nus.iss.profile_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.repository.MerchantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    public Optional<Merchant> getMerchant(UUID merchantId) {
        return merchantRepository.findByMerchantIdAndDeletedFalse(merchantId);
    }

    public Boolean updateMerchant(Merchant merchant) {
        merchantRepository.save(merchant);
        return true;
    }

    public boolean deleteMerchant(UUID merchantId) {
        merchantRepository.deleteById(merchantId);
        return true;
    }

    // implement a method to get merchants by emailID
    public Optional<Merchant> getMerchantByEmailId(String emailId) {
        return merchantRepository.findByMerchantEmailAndDeletedFalse(emailId);
    }

    public List<Merchant> getAllMerchants() {
     return merchantRepository.findAllByDeletedFalse();
    }

    public Merchant registerMerchant(Merchant merchant) {
        return merchantRepository.save(merchant);
    }

}