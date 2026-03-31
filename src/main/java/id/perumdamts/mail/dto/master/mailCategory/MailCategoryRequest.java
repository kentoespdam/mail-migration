package id.perumdamts.mail.dto.master.mailCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MailCategoryRequest(
        @NotNull @Positive Long mailTypeId,
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 64) String name,
        Integer sort
) {}
