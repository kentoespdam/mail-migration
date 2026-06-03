package id.perumdamts.mail.dto.master.messagetemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageTemplateRequest(
        @NotBlank String message,
        @Size(max = 128) String description
) {
}
