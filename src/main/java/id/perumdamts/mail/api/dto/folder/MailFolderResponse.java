package id.perumdamts.mail.api.dto.folder;

import id.perumdamts.mail.domain.enums.SystemFolder;

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
                sf == SystemFolder.ROOT || sf == SystemFolder.PERSONAL_ROOT ? 0 : 1,
                0,
                sf.name(),
                "email",
                true,
                0L,
                0L
        );
    }
}
