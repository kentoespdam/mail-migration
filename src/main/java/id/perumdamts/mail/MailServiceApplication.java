package id.perumdamts.mail;

import id.perumdamts.mail.config.AppWriteProperties;
import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.mcp.FolderTools;
import id.perumdamts.mail.mcp.MailTools;
import id.perumdamts.mail.mcp.MasterDataTools;
import id.perumdamts.mail.mcp.RecipientTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EnableConfigurationProperties({AppWriteProperties.class, TenantConfig.class, StorageProperties.class})
@EnableFeignClients(basePackages = "id.perumdamts.mail.integration")
@EnableAsync
@EnableScheduling
public class MailServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(MailServiceApplication.class, args);
    }

    @Bean
    ToolCallbackProvider mcpTools(MailTools mailTools,
                                  FolderTools folderTools,
                                  RecipientTools recipientTools,
                                  MasterDataTools masterDataTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mailTools, folderTools, recipientTools, masterDataTools)
                .build();
    }
}
