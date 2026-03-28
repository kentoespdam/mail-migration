package id.perumdamts.mail.dto.core.archive;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ArchiveAccessRequest(
        @NotEmpty List<AccessEntry> entries
) {
    public record AccessEntry(
            Integer positionId,
            Integer accessLevel
    ) {}
}
