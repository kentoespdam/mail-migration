package id.perumdamts.mail.dto.core.folder;

public record FolderCounterResponse(
        String folderId,
        String folderName,
        Long unread,
        Long total
) {}
