package id.perumdamts.mail.api.dto.folder;

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
