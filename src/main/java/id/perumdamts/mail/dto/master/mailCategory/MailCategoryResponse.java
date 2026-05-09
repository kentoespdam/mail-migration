package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailCategoryId;
import id.perumdamts.mail.dto.master.mailType.MailTypeMiniResponse;
import lombok.Value;

@Value
public class MailCategoryResponse implements HasSqid {
    MailCategoryId id;
    MailTypeMiniResponse mailType;
    String code;
    String name;
    String codeName;
    String status;
    Integer sort;
}
