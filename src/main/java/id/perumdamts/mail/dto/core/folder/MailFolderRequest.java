package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.id.MailFolderId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO untuk membuat atau mengubah mail folder.
 *
 * @param name           Nama folder
 * @param parentFolderId SQID dari parent folder (null jika root)
 */
public record MailFolderRequest(
                @NotBlank @Size(max = 45) String name,
                MailFolderId parentFolderId) {
}
