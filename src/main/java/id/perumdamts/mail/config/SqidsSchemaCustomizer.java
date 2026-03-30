package id.perumdamts.mail.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * Custom ModelConverter untuk memberitahu Swagger/OpenAPI bahwa field dengan
 * annotation @SqidId harus ditampilkan sebagai string, bukan integer.
 */
@Component
public class SqidsSchemaCustomizer implements ModelConverter {

    @Override
    public Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> next) {
        // Cek apakah field memiliki annotation @SqidId
        if (annotatedType.getCtxAnnotations() != null) {
            for (var annotation : annotatedType.getCtxAnnotations()) {
                if (annotation.annotationType().getName().equals("id.perumdamts.mail.infrastructure.sqids.SqidId")) {
                    StringSchema schema = new StringSchema();
                    schema.setExample("mtp_abc123");
                    schema.setDescription("SQID-encoded ID (string format)");
                    return schema;
                }
            }
        }
        
        // Delegasikan ke converter berikutnya untuk tipe lainnya
        if (next != null && next.hasNext()) {
            return next.next().resolve(annotatedType, context, next);
        }
        return null;
    }
}
