package id.perumdamts.mail.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Folder-folder sistem yang di-hardcode — sesuai {@code mail_folder.id} di DB legacy.
 *
 * <p>Folder sistem tidak bisa dihapus atau dipindah oleh user.
 * Gunakan enum ini (bukan magic int) untuk semua referensi ke ID folder sistem.
 */
public enum SystemFolder {

    ROOT(1),
    INBOX(2),
    DRAFT(3),
    READ(4),
    SENT(5),
    DELETED(6),
    PERSONAL_ROOT(10),
    PURGED(-1);

    private final int id;

    SystemFolder(int id) {
        this.id = id;
    }

    @JsonValue
    public int getId() {
        return id;
    }

    @JsonCreator
    public static SystemFolder fromId(int id) {
        for (SystemFolder folder : values()) {
            if (folder.id == id) return folder;
        }
        throw new IllegalArgumentException("Unknown SystemFolder id: " + id);
    }

    /** Apakah folder ini adalah folder sistem (tidak bisa dimodifikasi user)? */
    public boolean isSystemOwned() {
        return this != PERSONAL_ROOT && this != PURGED;
    }
}
