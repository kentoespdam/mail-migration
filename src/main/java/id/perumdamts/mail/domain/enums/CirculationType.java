package id.perumdamts.mail.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Jenis sirkulasi surat.
 *
 * <p>Nilai integer sesuai {@code sys_reference.code='sirkulasi'} di DB legacy.
 * Keputusan arsitektur: dijadikan Java enum (statis, dipakai di logika kode)
 * daripada lookup DB runtime.
 */
public enum CirculationType {

    DISPOSISI(1),
    MEMO_MANDIRI(2),
    MEMO(3),
    CC(4),
    REPLY(5),
    FORWARD(6);

    private final int dbValue;

    CirculationType(int dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public int getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static CirculationType fromDbValue(int value) {
        for (CirculationType type : values()) {
            if (type.dbValue == value) return type;
        }
        throw new IllegalArgumentException("Unknown CirculationType db value: " + value);
    }
}
