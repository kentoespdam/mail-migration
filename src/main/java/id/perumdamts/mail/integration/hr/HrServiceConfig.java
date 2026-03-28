package id.perumdamts.mail.integration.hr;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration for HR Service Feign client.
 * Handles error responses gracefully to avoid deserialization errors.
 */
@Configuration
public class HrServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(HrServiceConfig.class);

    @Bean
    public ErrorDecoder hrServiceErrorDecoder() {
        return new HrServiceErrorDecoder();
    }

    /**
     * Custom ErrorDecoder that returns empty list for batch endpoints
     * when HR service returns errors, preventing deserialization failures.
     */
    private static class HrServiceErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            try {
                int status = response.status();
                String reason = response.reason() != null ? response.reason() : "Unknown error";

                // Log the error
                String body = "";
                if (response.body() != null) {
                    byte[] bodyBytes = response.body().asInputStream().readAllBytes();
                    body = new String(bodyBytes, StandardCharsets.UTF_8);
                }
                log.warn("HR Service error for {}: {} {} - Body: {}", 
                        methodKey, status, reason, body.length() > 200 ? body.substring(0, 200) + "..." : body);

                // For 4xx/5xx errors, return a custom exception that fallback can handle
                if (status >= 500) {
                    return new RetryableException(
                            status,
                            reason,
                            response.request().httpMethod(),
                            null,
                            System.currentTimeMillis() + 5000,
                            response.request()
                    );
                }

                // For 4xx client errors, return exception with message
                return new HrServiceException("HR Service error: " + status + " " + reason, status);

            } catch (IOException e) {
                log.error("Error reading HR Service response", e);
                return new RuntimeException("Failed to read HR Service error response", e);
            }
        }
    }

    /**
     * Custom exception for HR Service errors.
     */
    @Getter
    public static class HrServiceException extends RuntimeException {
        private final int statusCode;

        public HrServiceException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

    }
}
