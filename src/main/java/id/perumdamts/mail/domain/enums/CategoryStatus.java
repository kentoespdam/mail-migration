package id.perumdamts.mail.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status untuk {@code mail_category.mcat_status}.
 * Legacy DB menyimpan sebagai enum('Enabled','Disabled','Deleted').
 */
public enum CategoryStatus {
    ENABLED("Enabled"),
    DISABLED("Disabled"),
    DELETED("Deleted");

    private final String dbValue;

    CategoryStatus(String dbValue) { this.dbValue = dbValue; }

    @JsonValue
    public String getDbValue() { return dbValue; }

    @JsonCreator
    public static CategoryStatus fromValue(String value) {
        for (CategoryStatus s : values()) {
            if (s.dbValue.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Unknown CategoryStatus: " + value);
    }
}
