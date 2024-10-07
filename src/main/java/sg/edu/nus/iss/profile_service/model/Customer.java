package sg.edu.nus.iss.profile_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@Data
@Entity
public class Customer implements Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID customerId;

    private String name;
    private String emailAddress;
    private String addressLine1;
    private String addressLine2;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
    private String phoneNumber;

    private boolean deleted = false;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be a 6-digit number")
    private String pincode;


    @Override
    public void createProfile() {

    }

    @Override
    public void updateProfile() {

    }

    @Override
    public void deleteProfile() {

    }
}