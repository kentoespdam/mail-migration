package id.perumdamts.mail.integration.hr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO untuk Jabatan dari HR Service.
 * Response HR Service memiliki nested structure dengan level.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JabatanDto(
        @JsonProperty("id") Long id,
        @JsonProperty("kode") String kode,
        @JsonProperty("nama") String nama,
        @JsonProperty("level") LevelDto level
) {
    public Long levelId() {
        return level != null ? level.id() : null;
    }
    
    public String levelNama() {
        return level != null ? level.nama() : null;
    }
}
