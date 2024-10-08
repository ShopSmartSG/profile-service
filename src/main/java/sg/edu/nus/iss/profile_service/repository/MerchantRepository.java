package sg.edu.nus.iss.profile_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.nus.iss.profile_service.model.Merchant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByEmailAddressAndDeletedFalse(String email);
    List<Merchant> findAllByDeletedFalse();
    Page<Merchant> findAllByDeletedFalse(Pageable pageable);
    Optional<Merchant> findByMerchantIdAndDeletedFalse(UUID id);



}