package id.perumdamts.mail.dto.master.messagetemplate;

import id.perumdamts.mail.dto.common.HasSqid;

public record MessageTemplateResponse(
        String id,
        String message,
        String description
) implements HasSqid {

    @Override
    public String getId() {
        return id();
    }
}
