package id.perumdamts.mail.event;

import java.time.Instant;

public record PublicationPublishedEvent(
        Integer publicationId,
        String publisherName,
        Instant publishedAt
) {
    public PublicationPublishedEvent(Integer publicationId, String publisherName) {
        this(publicationId, publisherName, Instant.now());
    }
}
