package id.perumdamts.mail.dto.master.quickMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuickMessageRequest(
        @NotBlank @Size(max = 128) String message
) {}
