package id.perumdamts.mail.api.dto.folder;

public record FolderCounterResponse(
        Integer folderId,
        String folderName,
        Long unread,
        Long total
) {}
