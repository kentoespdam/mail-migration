package id.perumdamts.mail.integration.hr;

/**
 * Ringkasan pegawai — dipakai untuk display di recipient card.
 */
public record EmployeeRingkasanDto(
        Long id,
        String nipam,
        String nama,
        String jabatanNama,
        String organisasiNama
) {}
