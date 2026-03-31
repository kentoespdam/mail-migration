package id.perumdamts.mail.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sqids")
public record SqidsProperties(
        String alphabet,
        int minLength,
        String separator,
        @NotBlank String shuffleKey
) {
    public SqidsProperties {
        if (alphabet == null || alphabet.isBlank())
            alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        if (minLength <= 0) minLength = 10;
        if (separator == null || separator.isBlank()) separator = "_";
    }
}
