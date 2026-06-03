package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.dto.id.MailId;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO untuk memindahkan mail antar folder.
 *
 * @param mailIds      List SQID mail yang akan dipindahkan
 * @param fromFolderId SQID folder asal
 * @param toFolderId   SQID folder tujuan
 */
public record MoveMailRequest(
                @NotEmpty List<MailId> mailIds,
                @NotNull MailFolderId fromFolderId,
                @NotNull MailFolderId toFolderId) {
}
