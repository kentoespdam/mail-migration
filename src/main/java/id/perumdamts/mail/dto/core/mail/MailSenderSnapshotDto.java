package id.perumdamts.mail.dto.core.mail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MailSenderSnapshotDto {
    private Long employeeId;
    private String fullName;
    private Long positionId;
    private String positionName;
    private Long unitId;
    private String unitName;
    private LocalDateTime capturedAt;
}
