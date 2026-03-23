package id.perumdamts.mail.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status pengiriman surat.
 *
 * <p>Nilai integer sesuai kolom {@code mail.status} di tabel legacy.
 */
public enum MailStatus {

    DRAFT(0),
    SENT(1);

    private final int dbValue;

    MailStatus(int dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public int getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static MailStatus fromDbValue(int value) {
        for (MailStatus status : values()) {
            if (status.dbValue == value) return status;
        }
        throw new IllegalArgumentException("Unknown MailStatus db value: " + value);
    }
}
