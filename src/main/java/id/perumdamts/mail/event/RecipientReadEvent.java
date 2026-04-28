package id.perumdamts.mail.event;

import java.time.Instant;

public record RecipientReadEvent(
        Long mailId,
        Long userId,
        Instant readAt) {
    public RecipientReadEvent(Long mailId, Long userId) {
        this(mailId, userId, Instant.now());
    }
}
