package id.perumdamts.mail.dto.core.response;

import java.time.LocalDate;

public record ResponseTimeFilterRequest(
        Long mailTypeId,
        Long mailCategoryId,
        Long unitId,
        LocalDate startDate,
        LocalDate endDate
) {
    public static ResponseTimeFilterRequest empty() {
        return new ResponseTimeFilterRequest(null, null, null, null, null);
    }
}