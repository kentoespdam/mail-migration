package id.perumdamts.mail.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReadStatus {

    UNREAD(0),
    READ(1);

    private final int dbValue;

    ReadStatus(int dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public int getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static ReadStatus fromDbValue(int value) {
        for (ReadStatus status : values()) {
            if (status.dbValue == value) return status;
        }
        throw new IllegalArgumentException("Unknown ReadStatus db value: " + value);
    }
}
