package id.perumdamts.mail.dto.core.response;

import java.time.LocalDate;

public record ResponseTimeStatsDto(
        Long count,
        Double avg,
        Double p50,
        Double p90,
        Double p99,
        Double min,
        Double max
) {
    public static ResponseTimeStatsDto empty() {
        return new ResponseTimeStatsDto(0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}