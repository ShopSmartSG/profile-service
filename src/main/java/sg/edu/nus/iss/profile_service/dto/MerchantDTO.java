package sg.edu.nus.iss.profile_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class MerchantDTO {

    private UUID merchantId;
    @NotBlank(message = "Merchant name is mandatory")
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String pincode;
    @NotBlank(message = "Merchant email is mandatory")
    @Email(message = "Email should be valid")
    private String emailAddress;
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
    private String phoneNumber;


}