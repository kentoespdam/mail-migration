package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.HasSqid;

/**
 * Response DTO untuk folder tree.
 * Include counter badge (unread, total) untuk menampilkan jumlah mail.
 *
 * @param id             SQID dari Mail folder
 * @param parentFolderId SQID dari parent folder (0 jika root)
 * @param ownerId        SQID dari owner user (0 jika system)
 * @param name           Nama folder
 * @param iconCls        CSS class untuk icon folder
 * @param system         Flag jika ini adalah system folder
 * @param unread         Jumlah mail yang belum dibaca
 * @param total          Total jumlah mail
 */
public record MailFolderResponse(
        String id,
        String parentFolderId,
        String ownerId,
        String name,
        String iconCls,
        boolean system,
        Long unread,
        Long total) implements HasSqid {

    @Override
    public String getId() {
        return id();
    }
}
