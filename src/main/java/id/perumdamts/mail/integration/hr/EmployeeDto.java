package id.perumdamts.mail.integration.hr;

/**
 * Representasi data pegawai dari HR Service.
 * userId == pegawaiId — tidak ada field mapping terpisah.
 */
public record EmployeeDto(
        Long id,
        String nipam,
        String nama,
        Long jabatanId,
        String jabatanNama,
        Long organisasiId,
        String organisasiNama,
        String statusKerja
) {}
