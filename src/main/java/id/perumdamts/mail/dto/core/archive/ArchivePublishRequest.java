package id.perumdamts.mail.dto.core.archive;

import jakarta.validation.constraints.NotBlank;

public record ArchivePublishRequest(
        @NotBlank(message = "Pattern must be provided (e.g., SHORT, LONG)") 
        String pattern
) {}
