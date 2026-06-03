package id.perumdamts.mail.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SqidIdInfrastructureTest {

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SqidsEncoder encoder;

    @Test
    void testConverter() {
        String sqid = encoder.encode(Mail.class, 123L);
        MailId mailId = conversionService.convert(sqid, MailId.class);

        assertThat(mailId).isNotNull();
        assertThat(mailId.value()).isEqualTo(123L);
    }

    @Test
    void testSerializer() throws Exception {
        MailId mailId = new MailId(123L);
        String json = objectMapper.writeValueAsString(mailId);
        String expectedSqid = encoder.encode(Mail.class, 123L);

        assertThat(json).isEqualTo("\"" + expectedSqid + "\"");
    }

    @Test
    void testDeserializer() throws Exception {
        String expectedSqid = encoder.encode(Mail.class, 123L);
        String json = "\"" + expectedSqid + "\"";
        MailId mailId = objectMapper.readValue(json, MailId.class);

        assertThat(mailId).isNotNull();
        assertThat(mailId.value()).isEqualTo(123L);
    }

    @Test
    void testInvalidSqidThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> conversionService.convert("invalid-sqid", MailId.class))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
