package id.perumdamts.mail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string", e);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        // This generic converter might not be ideal for deserialization without type info
        // But for our specific use case in Mail entity, we might need a specific one or use String
        return dbData;
    }
}
