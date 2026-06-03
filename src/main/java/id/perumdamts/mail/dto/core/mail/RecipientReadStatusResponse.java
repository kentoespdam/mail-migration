package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.id.MailRecipientId;
import id.perumdamts.mail.dto.id.UserId;

import java.time.LocalDateTime;

public record RecipientReadStatusResponse(MailRecipientId recipientId, UserId userId, String empName, String posName,
                                          String circulationName, Integer readStatus, LocalDateTime readDate) {
}
