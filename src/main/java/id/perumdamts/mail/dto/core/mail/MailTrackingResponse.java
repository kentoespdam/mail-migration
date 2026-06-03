package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.id.MailRecipientId;

import java.time.LocalDateTime;

public record MailTrackingResponse(
        MailRecipientId recipientId,
        String empName,
        String posName,
        String circulationName,
        Boolean isRead,
        LocalDateTime readDate,
        Integer depth,
        Boolean isRoot
) {
    public MailTrackingResponse {
        if (depth == null) depth = 0;
        if (isRoot == null) isRoot = false;
    }

    public static MailTrackingResponse root(MailRecipientId recipientId, String empName, String posName,
                                            String circulationName, Boolean isRead, LocalDateTime readDate) {
        return new MailTrackingResponse(recipientId, empName, posName, circulationName, isRead, readDate, 0, true);
    }

    public static MailTrackingResponse child(MailRecipientId recipientId, String empName, String posName,
                                            String circulationName, Boolean isRead, LocalDateTime readDate, int depth) {
        return new MailTrackingResponse(recipientId, empName, posName, circulationName, isRead, readDate, depth, false);
    }
}
