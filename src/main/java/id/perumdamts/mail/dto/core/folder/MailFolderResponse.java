package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.dto.id.UserId;

/**
 * Response DTO untuk folder tree.
 * Include counter badge (unread, total) untuk menampilkan jumlah mail.
 *
 * @param id             SQID dari Mail folder
 * @param parentFolderId SQID dari parent folder (null jika root)
 * @param ownerId        SQID dari owner user (null jika system)
 * @param name           Nama folder
 * @param iconCls        CSS class untuk icon folder
 * @param system         Flag jika ini adalah system folder
 * @param unread         Jumlah mail yang belum dibaca
 * @param total          Total jumlah mail
 */
public record MailFolderResponse(
        MailFolderId id,
        MailFolderId parentFolderId,
        UserId ownerId,
        String name,
        String iconCls,
        boolean system,
        Long unread,
        Long total) implements HasSqid {

    @Override
    public String getId() {
        return id != null ? id.toString() : null;
    }
}
