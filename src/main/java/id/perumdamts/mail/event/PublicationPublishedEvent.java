package id.perumdamts.mail.event;

import java.time.Instant;

public record PublicationPublishedEvent(
        Long publicationId,
        String publisherName,
        Instant publishedAt
) {
    public PublicationPublishedEvent(Long publicationId, String publisherName) {
        this(publicationId, publisherName, Instant.now());
    }
}
