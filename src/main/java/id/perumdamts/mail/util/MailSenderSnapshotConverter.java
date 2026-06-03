package id.perumdamts.mail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.perumdamts.mail.dto.core.mail.MailSenderSnapshotDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class MailSenderSnapshotConverter implements AttributeConverter<MailSenderSnapshotDto, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    public String convertToDatabaseColumn(MailSenderSnapshotDto attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting MailSenderSnapshotDto to JSON string", e);
            return null;
        }
    }

    @Override
    public MailSenderSnapshotDto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, MailSenderSnapshotDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON string to MailSenderSnapshotDto", e);
            return null;
        }
    }
}
