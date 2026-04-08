package id.perumdamts.mail.event;

import java.time.Instant;
import java.util.List;

public record MailSentEvent(
        Long mailId,
        Long senderId,
        String senderName,
        List<Long> recipientUserIds,
        Instant sentAt) {
    public MailSentEvent(Long mailId, Long senderId, String senderName, List<Long> recipientUserIds) {
        this(mailId, senderId, senderName, recipientUserIds, Instant.now());
    }
}
