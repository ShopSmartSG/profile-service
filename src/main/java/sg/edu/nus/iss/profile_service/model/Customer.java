package sg.edu.nus.iss.profile_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import sg.edu.nus.iss.profile_service.util.StringEncryptionConverter;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
public class Customer implements Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID customerId;

    @NotBlank(message = "Customer name is mandatory")
    @Convert(converter = StringEncryptionConverter.class)
    
    private String name;

    @NotBlank(message = "Customer email is mandatory")
    @Email(message = "Email should be valid")
    @Convert(converter = StringEncryptionConverter.class)
    
    private String emailAddress;

    @Convert(converter = StringEncryptionConverter.class)
    
    private String addressLine1;

    @Convert(converter = StringEncryptionConverter.class)
    
    private String addressLine2;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
    @Convert(converter = StringEncryptionConverter.class)
    
    private String phoneNumber;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be a 6-digit number")
    @Convert(converter = StringEncryptionConverter.class)
    
    private String pincode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    
    private Double latitude;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    
    private Double longitude;

    @JsonIgnore
    private boolean deleted = false;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    // TODO: This field is set to null in database , but to be set 0 in code , need to update db columns to put default values
    private BigDecimal rewardPoints = BigDecimal.ZERO;

    @Override
    public void createProfile() {
        // Implementation remains unchanged
    }

    @Override
    public void updateProfile() {
        // Implementation remains unchanged
    }

    @Override
    public void deleteProfile() {
        // Implementation remains unchanged
    }
}