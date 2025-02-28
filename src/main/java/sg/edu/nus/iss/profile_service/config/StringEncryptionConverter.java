package sg.edu.nus.iss.profile_service.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sg.edu.nus.iss.profile_service.service.EncryptionService;

@Converter
@Component
public class StringEncryptionConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Autowired
    public StringEncryptionConverter(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? encryptionService.encrypt(attribute) : null;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData != null ? encryptionService.decrypt(dbData) : null;
    }
}