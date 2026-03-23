package id.perumdamts.mail.domain.event;

import java.time.Instant;
import java.util.List;

public record ArchivePublishedEvent(
        Long archiveId,
        Integer publisherId,
        String publisherName,
        String officeCode,
        List<Integer> accessPositionIds,
        Instant publishedAt
) {
    public ArchivePublishedEvent(Long archiveId, Integer publisherId, String publisherName,
                                  String officeCode, List<Integer> accessPositionIds) {
        this(archiveId, publisherId, publisherName, officeCode, accessPositionIds, Instant.now());
    }
}
