package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.infrastructure.sqids.SqidId;
import id.perumdamts.mail.infrastructure.sqids.SqidPrefix;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MailCategoryRequest(
        @NotNull @Positive @SqidId(SqidPrefix.MAIL_TYPE) Integer mailTypeId,
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 64) String name,
        Integer sort
) {}
