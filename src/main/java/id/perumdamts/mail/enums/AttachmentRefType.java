package id.perumdamts.mail.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Tipe referensi attachment — polymorphic owner.
 * Legacy DB: {@code ref_type} integer (1=Mail, 2=Arsip).
 */
public enum AttachmentRefType {

    MAIL(1),
    ARCHIVE(2);

    private final int dbValue;

    AttachmentRefType(int dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public int getDbValue() { return dbValue; }

    @JsonCreator
    public static AttachmentRefType fromDbValue(int value) {
        for (AttachmentRefType type : values()) {
            if (type.dbValue == value) return type;
        }
        throw new IllegalArgumentException("Unknown AttachmentRefType: " + value);
    }
}
