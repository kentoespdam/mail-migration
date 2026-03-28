package id.perumdamts.mail.dto.core.publication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePublicationRequest(
        @NotBlank String title,
        String description,
        @NotNull Integer documentTypeId,
        boolean publish
) {}
