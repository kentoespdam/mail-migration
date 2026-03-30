package id.perumdamts.mail.infrastructure.sqids;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

public class SqidIdSerializer extends JsonSerializer<Number> implements ContextualSerializer {

    private SqidPrefix prefix;
    private SqidsHelper sqidsHelper;

    public SqidIdSerializer() {}

    public SqidIdSerializer(SqidPrefix prefix, SqidsHelper sqidsHelper) {
        this.prefix = prefix;
        this.sqidsHelper = sqidsHelper;
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(sqidsHelper.encode(prefix, value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        SqidId annotation = property.getAnnotation(SqidId.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(SqidId.class);
        }
        if (annotation != null) {
            SqidsHelper helper = (SqidsHelper) prov.getAttribute(SqidsHelper.class);
            return new SqidIdSerializer(annotation.value(), helper);
        }
        return this;
    }
}
