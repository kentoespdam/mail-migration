package id.perumdamts.mail.dto.core.publication;

import jakarta.validation.constraints.NotBlank;

public record CreatePublicationRequest(
        @NotBlank String title,
        String description,
        @NotBlank String documentTypeId,
        boolean publish
) {
}
