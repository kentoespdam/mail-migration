package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.enums.SystemFolder;

/**
 * Response DTO untuk folder tree.
 * Include counter badge (unread, total) untuk menampilkan jumlah mail.
 */
public record MailFolderResponse(
        String id,
        String parentFolderId,
        String ownerId,
        String name,
        String iconCls,
        boolean system,
        Long unread,
        Long total) {
    public static MailFolderResponse fromSystemFolder(SystemFolder sf) {
        return new MailFolderResponse(
                String.valueOf(sf.getId()),
                sf.getParent() != null ? String.valueOf(sf.getParent().getId()) : "0",
                "0",
                sf.getDisplayName(),
                "email",
                true,
                0L,
                0L);
    }
}
