package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.HasSqid;

/**
 * Response DTO untuk counter folder.
 *
 * @param folderId   SQID dari folder
 * @param folderName Nama folder
 * @param unread     Jumlah mail yang belum dibaca
 * @param total      Total jumlah mail
 */
public record FolderCounterResponse(
                String folderId,
                String folderName,
                Long unread,
                Long total) implements HasSqid {
        @Override
        public String getId() {
                return folderId();
        }
}
