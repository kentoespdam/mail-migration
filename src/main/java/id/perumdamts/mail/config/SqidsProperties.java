package id.perumdamts.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.sqids")
public record SqidsProperties(
        String alphabet,
        int minLength,
        String shuffleKey
) {}
