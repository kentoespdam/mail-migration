package id.perumdamts.mail.api.dto.publication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePublicationRequest(
        @NotBlank String title,
        String description,
        @NotNull Integer documentTypeId,
        boolean publish
) {}
