package id.perumdamts.mail.infrastructure.sqids;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class SqidIdDeserializer extends JsonDeserializer<Integer> implements ContextualDeserializer {

    private SqidsHelper sqidsHelper;

    public SqidIdDeserializer() {}

    public SqidIdDeserializer(SqidsHelper sqidsHelper) {
        this.sqidsHelper = sqidsHelper;
    }

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String sqid = p.getValueAsString();
        if (sqid == null || sqid.isBlank()) {
            return null;
        }
        return sqidsHelper.decode(sqid);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        SqidsHelper helper = (SqidsHelper) ctxt.getAttribute(SqidsHelper.class);
        return new SqidIdDeserializer(helper);
    }
}
