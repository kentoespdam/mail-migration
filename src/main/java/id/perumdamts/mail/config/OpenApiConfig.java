package id.perumdamts.mail.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "Bearer Token";
        return new OpenAPI()
                .info(new Info()
                        .title("Mail Service API")
                        .description("API untuk manajemen surat digital PERUMDAMTS")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PERUMDAMTS")
                                .email("admin@perumdamts.id")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .schemaRequirement(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }

    @Bean
    public GroupedOpenApi masterApi() {
        return GroupedOpenApi.builder()
                .group("master")
                .displayName("Master Data")
                .pathsToMatch(
                        "/api/v1/mail-types/**",
                        "/api/v1/mail-categories/**",
                        "/api/v1/quick-messages/**",
                        "/api/v1/file-rules/**",
                        "/api/v1/document-types/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi coreApi() {
        return GroupedOpenApi.builder()
                .group("core")
                .displayName("Core Mail")
                .pathsToMatch(
                        "/api/v1/mails/**",
                        "/api/v1/mail/**",
                        "/api/v1/attachments/**",
                        "/api/v1/archives/**",
                        "/api/v1/publications/**"
                )
                .build();
    }
}
