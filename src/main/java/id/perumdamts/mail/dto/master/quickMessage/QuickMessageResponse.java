package id.perumdamts.mail.dto.master.quickMessage;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class QuickMessageResponse implements HasSqid {
    String id;
    String message;
}
