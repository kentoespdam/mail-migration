package id.perumdamts.mail.dto.core.folder;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MoveMailRequest(
        @NotNull List<String> mailIds,
        @NotNull String fromFolderId,
        @NotNull String toFolderId
) {}
