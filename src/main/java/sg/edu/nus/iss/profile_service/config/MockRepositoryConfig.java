package sg.edu.nus.iss.profile_service.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import sg.edu.nus.iss.profile_service.repository.CustomerRepository;
import sg.edu.nus.iss.profile_service.repository.DeliveryPartnerRepository;
import sg.edu.nus.iss.profile_service.repository.MerchantRepository;

@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "zapscan")
public class MockRepositoryConfig {

    @Bean
    @Primary
    public MerchantRepository merchantRepository() {
        return Mockito.mock(MerchantRepository.class);
    }

    @Bean
    @Primary
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

    @Bean
    @Primary
    public DeliveryPartnerRepository deliveryPartnerRepository() {
        return Mockito.mock(DeliveryPartnerRepository.class);
    }
} 