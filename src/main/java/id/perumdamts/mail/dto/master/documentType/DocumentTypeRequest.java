package id.perumdamts.mail.dto.master.documentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentTypeRequest(
        @NotBlank @Size(max = 100) String name
) {}
