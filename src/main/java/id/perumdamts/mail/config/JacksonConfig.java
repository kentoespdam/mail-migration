package id.perumdamts.mail.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(SqidIdJsonModule sqidIdJsonModule) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(sqidIdJsonModule);
        return mapper;
    }
}
