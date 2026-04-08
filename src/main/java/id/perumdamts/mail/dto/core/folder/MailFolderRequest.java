package id.perumdamts.mail.dto.core.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO untuk membuat atau mengubah mail folder.
 *
 * @param name           Nama folder
 * @param parentFolderId SQID dari parent folder (pake "0" jika root)
 */
public record MailFolderRequest(
                @NotBlank @Size(max = 45) String name,
                @NotBlank String parentFolderId) {
}
