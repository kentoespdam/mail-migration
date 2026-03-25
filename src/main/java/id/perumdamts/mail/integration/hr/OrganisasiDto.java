package id.perumdamts.mail.integration.hr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO untuk Organisasi dari HR Service.
 * Response HR Service memiliki field shortName.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrganisasiDto(
        @JsonProperty("id") Long id,
        @JsonProperty("kode") String kode,
        @JsonProperty("nama") String nama,
        @JsonProperty("shortName") String shortName
) {}
