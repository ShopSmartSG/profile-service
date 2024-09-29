package sg.edu.nus.iss.profile_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.nus.iss.profile_service.model.Merchant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByMerchantEmail(String emailId);

    Optional<Merchant> findByMerchantEmailAndDeletedFalse(String email);
    List<Merchant> findAllByDeletedFalse();
    Optional<Merchant> findByMerchantIdAndDeletedFalse(UUID id);
}