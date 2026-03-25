package id.perumdamts.mail.integration.hr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wrapper response untuk HR Service endpoints.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeResponse(
        @JsonProperty("data") List<EmployeeDto> data,
        @JsonProperty("status") Integer status,
        @JsonProperty("statusText") String statusText,
        @JsonProperty("errors") List<String> errors,
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") String timestamp
) {
    public List<EmployeeDto> getData() {
        return data != null ? data : List.of();
    }
}
