package id.perumdamts.mail.dto.master.quickMessage;

import id.perumdamts.mail.dto.id.QuickMessageId;
import lombok.Value;

@Value
public class QuickMessageResponse {
    QuickMessageId id;
    String message;
}
