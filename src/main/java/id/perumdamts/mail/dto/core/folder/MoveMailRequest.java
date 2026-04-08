package id.perumdamts.mail.dto.core.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO untuk memindahkan mail antar folder.
 *
 * @param mailIds      List SQID mail yang akan dipindahkan
 * @param fromFolderId SQID folder asal
 * @param toFolderId   SQID folder tujuan
 */
public record MoveMailRequest(
                @NotEmpty List<String> mailIds,
                @NotBlank String fromFolderId,
                @NotBlank String toFolderId) {
}
