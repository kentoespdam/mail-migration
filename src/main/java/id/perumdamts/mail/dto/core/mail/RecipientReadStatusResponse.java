package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDateTime;

public record RecipientReadStatusResponse(String recipientId, String userId, String empName, String posName,
                                          String circulationName, Integer readStatus, LocalDateTime readDate) {
}
