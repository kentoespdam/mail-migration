package id.perumdamts.mail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Alias bean agar SpEL {@code @appWriteProperties} bisa resolve di @Cacheable.
 * Bean dari @EnableConfigurationProperties punya nama panjang (prefix-FQCN).
 */
@Configuration
public class AppWritePropertiesConfig {

    @Primary
    @Bean("appWriteProperties")
    public AppWriteProperties appWriteProperties(AppWriteProperties props) {
        return props;
    }
}
