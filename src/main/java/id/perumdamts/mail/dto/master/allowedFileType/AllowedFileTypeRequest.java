package id.perumdamts.mail.dto.master.allowedFileType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AllowedFileTypeRequest(
        @NotBlank String context,
        @NotBlank String extension,
        @NotNull @Positive Integer maxSizeMb,
        Boolean isActive
) {}
