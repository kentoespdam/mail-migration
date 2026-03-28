package id.perumdamts.mail.event;

import java.time.Instant;
import java.util.List;

public record MailSentEvent(
        Integer mailId,
        Integer senderId,
        String senderName,
        List<Integer> recipientUserIds,
        Instant sentAt
) {
    public MailSentEvent(Integer mailId, Integer senderId, String senderName, List<Integer> recipientUserIds) {
        this(mailId, senderId, senderName, recipientUserIds, Instant.now());
    }
}
