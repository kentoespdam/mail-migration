package id.perumdamts.mail.api.dto.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MailFolderRequest(
        @NotBlank @Size(max = 45) String name,
        @NotNull Integer parentFolderId
) {}
