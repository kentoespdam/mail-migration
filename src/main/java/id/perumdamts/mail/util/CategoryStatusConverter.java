package id.perumdamts.mail.util;

import id.perumdamts.mail.enums.CategoryStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryStatusConverter implements AttributeConverter<CategoryStatus, String> {

    @Override
    public String convertToDatabaseColumn(CategoryStatus attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public CategoryStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CategoryStatus.fromValue(dbData);
    }
}
