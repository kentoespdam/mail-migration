package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.infrastructure.sqids.SqidId;
import id.perumdamts.mail.infrastructure.sqids.SqidPrefix;

public record QuickMessageResponse(
        @SqidId(SqidPrefix.QUICK_MESSAGE) Integer id,
        String message
) {}
