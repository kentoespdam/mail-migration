package id.perumdamts.mail.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PublicationStatus {

    DRAFT(1),
    PUBLISHED(2),
    DELETED(3);

    private final int legacyCode;

    PublicationStatus(int legacyCode) {
        this.legacyCode = legacyCode;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static PublicationStatus fromValue(String value) {
        for (PublicationStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) return status;
        }
        throw new IllegalArgumentException("Unknown PublicationStatus: " + value);
    }

}
