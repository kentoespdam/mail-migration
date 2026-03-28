package id.perumdamts.mail.dto.core.folder;

public record FolderCounterResponse(
        Integer folderId,
        String folderName,
        Long unread,
        Long total
) {}
