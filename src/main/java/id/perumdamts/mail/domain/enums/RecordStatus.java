package id.perumdamts.mail.domain.enums;

/**
 * Status lifecycle untuk semua entity yang mendukung soft-delete.
 *
 * <p>Dipakai bersama {@code @SQLRestriction("status != 'DELETED'")} pada JPA entity
 * agar record yang di-delete tidak muncul di query normal.
 */
public enum RecordStatus {
    ACTIVE,
    INACTIVE,
    DELETED
}
