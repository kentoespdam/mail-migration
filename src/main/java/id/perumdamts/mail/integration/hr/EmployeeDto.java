package id.perumdamts.mail.integration.hr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representasi data pegawai dari HR Service.
 * userId == pegawaiId — tidak ada field mapping terpisah.
 * Note: Response dari HR Service memiliki nested structure untuk jabatan dan organisasi.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeDto(
        @JsonProperty("id") Long id,
        @JsonProperty("nipam") String nipam,
        @JsonProperty("nama") String nama,
        @JsonProperty("statusPegawai") String statusPegawai,
        @JsonProperty("jabatan") JabatanDto jabatan,
        @JsonProperty("organisasi") OrganisasiDto organisasi,
        @JsonProperty("golongan") GolonganDto golongan
) {
    public Long jabatanId() {
        return jabatan != null ? jabatan.id() : null;
    }
    
    public String jabatanNama() {
        return jabatan != null ? jabatan.nama() : null;
    }
    
    public Long organisasiId() {
        return organisasi != null ? organisasi.id() : null;
    }
    
    public String organisasiNama() {
        return organisasi != null ? organisasi.nama() : null;
    }
    
    // Map statusPegawai to statusKerja untuk backward compatibility
    public String statusKerja() {
        return statusPegawai;
    }
}
