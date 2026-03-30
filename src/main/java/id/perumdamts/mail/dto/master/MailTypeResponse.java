package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.infrastructure.sqids.SqidId;
import id.perumdamts.mail.infrastructure.sqids.SqidPrefix;

public record MailTypeResponse(
        @SqidId(SqidPrefix.MAIL_TYPE) Integer id,
        String name,
        RecordStatus status,
        long categoryCount
) {}
