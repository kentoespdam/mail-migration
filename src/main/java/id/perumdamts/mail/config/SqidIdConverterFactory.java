package id.perumdamts.mail.config;

import id.perumdamts.mail.dto.id.*;
import id.perumdamts.mail.dto.id.marker.Employee;
import id.perumdamts.mail.dto.id.marker.Office;
import id.perumdamts.mail.dto.id.marker.Position;
import id.perumdamts.mail.dto.id.marker.User;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.entity.master.AllowedFileType;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.entity.master.MessageTemplate;
import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.util.SqidsEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqidIdConverterFactory implements ConverterFactory<String, SqidId> {

    private final SqidsEncoder encoder;

    private static final Map<Class<? extends SqidId>, Class<?>> TYPE_MAP = Map.ofEntries(
            Map.entry(MailId.class, Mail.class),
            Map.entry(MailTypeId.class, MailType.class),
            Map.entry(MailCategoryId.class, MailCategory.class),
            Map.entry(AttachmentId.class, Attachment.class),
            Map.entry(PublicationId.class, Publication.class),
            Map.entry(DocumentTypeId.class, DocumentType.class),
            Map.entry(AllowedFileTypeId.class, AllowedFileType.class),
            Map.entry(QuickMessageId.class, QuickMessage.class),
            Map.entry(MessageTemplateId.class, MessageTemplate.class),
            Map.entry(EmployeeId.class, Employee.class),
            Map.entry(OfficeId.class, Office.class),
            Map.entry(PositionId.class, Position.class),
            Map.entry(UserId.class, User.class)
    );

    @Override
    public <T extends SqidId> Converter<String, T> getConverter(Class<T> targetType) {
        return new SqidIdConverter<>(targetType, encoder);
    }

    private record SqidIdConverter<T extends SqidId>(Class<T> targetType, SqidsEncoder encoder) implements Converter<String, T> {

        @Override
        public T convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }

            Class<?> entityClass = TYPE_MAP.get(targetType);
            if (entityClass == null) {
                throw new IllegalArgumentException("Unsupported SqidId type: " + targetType.getName());
            }

            long value = encoder.decode(entityClass, source);
            try {
                return targetType.getConstructor(long.class).newInstance(value);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to instantiate " + targetType.getSimpleName(), e);
            }
        }
    }
}
