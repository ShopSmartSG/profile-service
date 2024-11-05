package sg.edu.nus.iss.profile_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.DeliveryPartner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {

    Optional<DeliveryPartner> findByEmailAddressAndDeletedFalse(String email);
    List<DeliveryPartner> findAllByDeletedFalse();
    Page<DeliveryPartner> findAllByDeletedFalse(Pageable pageable);
    Optional<DeliveryPartner> findByDeliveryPartnerIdAndDeletedFalse(UUID id);


}
