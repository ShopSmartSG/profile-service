package sg.edu.nus.iss.profile_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.nus.iss.profile_service.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmailAddressAndDeletedFalse(String email);
    List<Customer> findAllByDeletedFalse();
    Optional<Customer> findByCustomerIdAndDeletedFalse(UUID id);
}