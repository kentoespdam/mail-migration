package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.id.PublicationId;
import java.time.LocalDateTime;

public record PublicationCommandResult(
        PublicationId id,
        String status,
        LocalDateTime timestamp
) {
}
