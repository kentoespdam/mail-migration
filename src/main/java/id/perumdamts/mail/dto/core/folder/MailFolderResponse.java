package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.enums.SystemFolder;

/**
 * Response DTO untuk folder tree.
 * Include counter badge (unread, total) untuk menampilkan jumlah mail.
 */
public record MailFolderResponse(
        Integer id,
        Integer parentFolderId,
        Integer ownerId,
        String name,
        String iconCls,
        boolean system,
        Long unread,
        Long total
) {
    public static MailFolderResponse fromSystemFolder(SystemFolder sf) {
        return new MailFolderResponse(
                sf.getId(),
                sf.getParent() != null ? sf.getParent().getId() : 0,
                0,
                sf.getDisplayName(),
                "email",
                true,
                0L,
                0L
        );
    }
}
