package id.perumdamts.mail.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status arsip surat.
 *
 * <p>Nilai integer sesuai kolom {@code mail_archive.status} di tabel legacy.
 */
public enum ArchiveStatus {

    DRAFT(1),
    ARCHIVED(2),
    DELETED(3);

    private final int dbValue;

    ArchiveStatus(int dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public int getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static ArchiveStatus fromDbValue(int value) {
        for (ArchiveStatus status : values()) {
            if (status.dbValue == value) return status;
        }
        throw new IllegalArgumentException("Unknown ArchiveStatus db value: " + value);
    }
}
