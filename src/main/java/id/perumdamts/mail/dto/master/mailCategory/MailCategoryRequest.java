package id.perumdamts.mail.dto.master.mailCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MailCategoryRequest(
        @NotBlank String mailTypeId,
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 64) String name,
        Integer sort
) {}
