package id.perumdamts.mail.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.perumdamts.mail.infrastructure.sqids.SqidsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(SqidsHelper sqidsHelper) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setConfig(
                objectMapper.getSerializationConfig().withAttribute(SqidsHelper.class, sqidsHelper));
        objectMapper.setConfig(
                objectMapper.getDeserializationConfig().withAttribute(SqidsHelper.class, sqidsHelper));
        return objectMapper;
    }
}
