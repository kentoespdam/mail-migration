package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.core.Attachment;

public record AttachmentId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return Attachment.class;
    }
}
