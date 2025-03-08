package sg.edu.nus.iss.profile_service.util;

import org.springframework.stereotype.Component;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.Merchant;

@Component
public class LogMasker {

    public String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        return username.charAt(0) + "****" + username.charAt(username.length() - 1) + domain;
    }

    public String maskEntity(Object entity) {
        if (entity == null) {
            return "null";
        }

        if (entity instanceof Customer) {
            Customer customer = (Customer) entity;
            return String.format("Customer(id=%s, name=%s, email=%s, ...)",
                    customer.getCustomerId(),
                    maskString(customer.getName()),
                    maskEmail(customer.getEmailAddress()));
        }

        if (entity instanceof Merchant) {
            Merchant merchant = (Merchant) entity;
            return String.format("Merchant(id=%s, name=%s, email=%s, ...)",
                    merchant.getMerchantId(),
                    maskString(merchant.getName()),
                    maskEmail(merchant.getEmailAddress()));
        }

        // Add other entity types as needed

        return entity.toString();
    }

    private String maskString(String value) {
        if (value == null || value.length() <= 2) {
            return "****";
        }
        return value.charAt(0) + "****" + value.charAt(value.length() - 1);
    }
}
