package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailCategoryId;

public record MailCategoryLookup(MailCategoryId id, String name) implements HasSqid {
    @Override
    public MailCategoryId getId() {
        return id;
    }
}
