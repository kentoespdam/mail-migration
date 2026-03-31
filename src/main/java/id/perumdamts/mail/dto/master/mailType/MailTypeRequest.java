package id.perumdamts.mail.dto.master.mailType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MailTypeRequest(
        @NotBlank @Size(max = 32) String name
) {}
