package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class MailCategoryResponse implements HasSqid {
    String id;
    String mailTypeSqid;
    String mailTypeName;
    String code;
    String name;
    String codeName;
    String status;
    Integer sort;
}
