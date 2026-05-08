package id.perumdamts.mail.dto.integration.hr;

import lombok.Data;

@Data
public class HrCacheInvalidationRequest {
    private String type; // e.g., EMPLOYEE
    private String id;   // e.g., 123
    private String action; // e.g., UPDATE, DELETE
}
