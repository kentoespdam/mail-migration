package id.perumdamts.mail.dto.master.messagetemplate;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MessageTemplateId;

public record MessageTemplateResponse(
        MessageTemplateId id,
        String message,
        String description
) implements HasSqid {

    @Override
    public MessageTemplateId getId() {
        return id();
    }
}
