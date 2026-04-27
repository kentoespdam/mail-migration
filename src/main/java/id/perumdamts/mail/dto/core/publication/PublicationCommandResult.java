package id.perumdamts.mail.dto.core.publication;

import java.time.LocalDateTime;

public record PublicationCommandResult(
        String id,
        String status,
        LocalDateTime timestamp
) {
}
