package sg.edu.nus.iss.profile_service.util;

import org.springframework.stereotype.Component;
import sg.edu.nus.iss.profile_service.model.Customer;
import sg.edu.nus.iss.profile_service.model.DeliveryPartner;
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
            return String.format("Customer(id=%s, name=%s, email=%s, phone=%s, address1=%s, address2=%s, pincode=%s, lat=%s, long=%s)",
                    customer.getCustomerId(),
                    maskString(customer.getName()),
                    maskEmail(customer.getEmailAddress()),
                    maskString(customer.getPhoneNumber()),
                    maskString(customer.getAddressLine1()),
                    maskString(customer.getAddressLine2()),
                    maskString(customer.getPincode()),
                    maskString(String.valueOf(customer.getLatitude())),
                    maskString(String.valueOf(customer.getLongitude())));


        }else if (entity instanceof Merchant) {
            Merchant merchant = (Merchant) entity;
            return String.format("Customer(id=%s, name=%s, email=%s, phone=%s, address1=%s, address2=%s, pincode=%s, lat=%s, long=%s)",
                    merchant.getMerchantId(),
                    maskString(merchant.getName()),
                    maskEmail(merchant.getEmailAddress()),
                    maskString(merchant.getPhoneNumber()),
                    maskString(merchant.getAddressLine1()),
                    maskString(merchant.getAddressLine2()),
                    maskString(merchant.getPincode()),
                    maskString(String.valueOf(merchant.getLatitude())),
                    maskString(String.valueOf(merchant.getLongitude())));
        }else if (entity instanceof DeliveryPartner) {
            DeliveryPartner deliveryPartner = (DeliveryPartner) entity;
            return String.format("Customer(id=%s, name=%s, email=%s, phone=%s, address1=%s, address2=%s, pincode=%s, lat=%s, long=%s)",
                    deliveryPartner.getDeliveryPartnerId(),
                    maskString(deliveryPartner.getName()),
                    maskEmail(deliveryPartner.getEmailAddress()),
                    maskString(deliveryPartner.getPhoneNumber()),
                    maskString(deliveryPartner.getAddressLine1()),
                    maskString(deliveryPartner.getAddressLine2()),
                    maskString(deliveryPartner.getPincode()),
                    maskString(String.valueOf(deliveryPartner.getLatitude())),
                    maskString(String.valueOf(deliveryPartner.getLongitude())));
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
