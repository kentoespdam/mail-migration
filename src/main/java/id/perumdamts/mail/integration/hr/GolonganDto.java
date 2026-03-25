package id.perumdamts.mail.integration.hr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO untuk Golongan dari HR Service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GolonganDto(
        @JsonProperty("id") Long id,
        @JsonProperty("nama") String nama
) {}
