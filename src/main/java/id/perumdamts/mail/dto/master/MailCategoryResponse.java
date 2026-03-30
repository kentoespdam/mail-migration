package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.infrastructure.sqids.SqidId;
import id.perumdamts.mail.infrastructure.sqids.SqidPrefix;

public record MailCategoryResponse(
        @SqidId(SqidPrefix.MAIL_CATEGORY) Integer id,
        @SqidId(SqidPrefix.MAIL_TYPE) Integer mailTypeId,
        String mailTypeName,
        String code,
        String name,
        String codeName,
        String status,
        Integer sort
) {}
