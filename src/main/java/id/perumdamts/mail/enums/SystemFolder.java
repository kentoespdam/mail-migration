package id.perumdamts.mail.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Optional;

/**
 * Folder-folder sistem yang di-hardcode — sesuai {@code mail_folder.id} di DB legacy.
 *
 * <p>Folder sistem tidak bisa dihapus atau dipindah oleh user.
 * Gunakan enum ini (bukan magic int) untuk semua referensi ke ID folder sistem.
 */
@Getter
public enum SystemFolder {

    ROOT(1, "eOffice Mailbox", null),
    INBOX(2, "Inbox", ROOT),
    DRAFT(3, "Draft", ROOT),
    READ(4, "Read Items", ROOT),
    SENT(5, "Sent Items", ROOT),
    DELETED(6, "Deleted Items", ROOT),
    PERSONAL_ROOT(10, "Personal Folder", null),
    PURGED(-1, "Purged", null);

    @JsonValue
    private final int id;
    private final String displayName;
    private final SystemFolder parent;

    SystemFolder(int id, String displayName, SystemFolder parent) {
        this.id = id;
        this.displayName = displayName;
        this.parent = parent;
    }

    /** Folder yang membutuhkan JOIN ke mail_recipient + sirkulasi */
    public boolean requiresRecipientJoin() {
        return this == INBOX || this == READ || this == DELETED;
    }

    /** Folder yang ditampilkan di counter badge */
    public boolean isCountable() {
        return this != ROOT && this != PERSONAL_ROOT && this != PURGED;
    }

    /** Apakah folder ini valid sebagai tujuan move oleh user */
    public boolean isMovable() {
        return this == READ || this == DELETED;
    }

    /** Apakah folder ini adalah folder sistem (tidak bisa dimodifikasi user)? */
    public boolean isSystemOwned() {
        return this != PERSONAL_ROOT && this != PURGED;
    }

    /** Cek apakah folderId adalah personal folder (id > PERSONAL_ROOT) */
    public static boolean isPersonalFolder(int folderId) {
        return folderId > PERSONAL_ROOT.id;
    }

    @JsonCreator
    public static SystemFolder fromId(int id) {
        for (SystemFolder folder : values()) {
            if (folder.id == id) return folder;
        }
        throw new IllegalArgumentException("Unknown SystemFolder id: " + id);
    }

    public static Optional<SystemFolder> findById(int id) {
        for (SystemFolder folder : values()) {
            if (folder.id == id) return Optional.of(folder);
        }
        return Optional.empty();
    }
}
