package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailCategoryId;
import id.perumdamts.mail.dto.master.mailType.MailTypeMiniResponse;

public record MailCategoryResponse(
    MailCategoryId id,
    MailTypeMiniResponse mailType,
    String code,
    String name,
    String codeName,
    String status,
    Integer sort
) implements HasSqid {
    @Override
    public MailCategoryId getId() {
        return id;
    }
}
