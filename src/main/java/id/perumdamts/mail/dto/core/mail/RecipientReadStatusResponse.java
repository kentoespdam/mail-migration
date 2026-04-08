package id.perumdamts.mail.dto.core.mail;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class RecipientReadStatusResponse {
    String recipientId;
    String userId;
    String empName;
    String posName;
    String circulationName;
    Integer readStatus;
    LocalDateTime readDate;
}
