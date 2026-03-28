package id.perumdamts.mail.dto.core.folder;

/**
 * DTO untuk counter badge per folder.
 * Digunakan untuk menampilkan jumlah unread dan total mail.
 */
public record FolderCountDto(
        Integer folderId,
        String folderName,
        Long unread,
        Long total
) {}
