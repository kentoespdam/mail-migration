package id.perumdamts.mail;

import id.perumdamts.mail.config.AppWriteProperties;
import id.perumdamts.mail.config.TenantConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Mail Service — migrasi surat-menyurat dari PHP ke Spring Boot 4.
 *
 * <p>Annotation penting:
 * <ul>
 *   <li>{@code @EnableFeignClients} — aktifkan OpenFeign untuk HR Service client</li>
 *   <li>{@code @EnableAsync} — aktifkan async execution untuk domain event listeners</li>
 * </ul>
 *
 * <p>{@code @EnableCaching} dan {@code @EnableScheduling} dikonfigurasi di class config masing-masing.
 */
@SpringBootApplication
@EnableConfigurationProperties({AppWriteProperties.class, TenantConfig.class})
@EnableFeignClients(basePackages = "id.perumdamts.mail.integration")
@EnableAsync
public class MailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailServiceApplication.class, args);
    }
}
