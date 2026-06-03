package id.perumdamts.mail.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqidIdJsonModule extends SimpleModule {

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
    public void setupModule(SetupContext context) {
        Serializer serializer = new Serializer();
        Deserializer deserializer = new Deserializer();

        for (Class<? extends SqidId> clazz : TYPE_MAP.keySet()) {
            addSerializer((Class<SqidId>) clazz, serializer);
            addDeserializer((Class<SqidId>) clazz, (JsonDeserializer) deserializer);
        }

        super.setupModule(context);
    }

    private class Serializer extends JsonSerializer<SqidId> {
        @Override
        public void serialize(SqidId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            String encoded = encoder.encode(value.getEntityClass(), value.value());
            gen.writeString(encoded);
        }
    }

    private class Deserializer extends JsonDeserializer<SqidId> implements ContextualDeserializer {

        private final Class<? extends SqidId> targetType;

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<? extends SqidId> targetType) {
            this.targetType = targetType;
        }

        @Override
        public SqidId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String source = p.getValueAsString();
            if (source == null || source.isBlank()) {
                return null;
            }

            Class<? extends SqidId> type = targetType != null ? targetType : (Class<? extends SqidId>) ctxt.getContextualType().getRawClass();

            Class<?> entityClass = TYPE_MAP.get(type);
            if (entityClass == null) {
                throw new IOException("Unsupported SqidId type: " + type.getName());
            }

            long value = encoder.decode(entityClass, source);
            try {
                return type.getConstructor(long.class).newInstance(value);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IOException("Failed to instantiate " + type.getSimpleName(), e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            if (ctxt.getContextualType() == null) {
                return this;
            }
            Class<?> rawClass = ctxt.getContextualType().getRawClass();
            if (SqidId.class.isAssignableFrom(rawClass)) {
                return new Deserializer((Class<? extends SqidId>) rawClass);
            }
            return this;
        }
    }
}
