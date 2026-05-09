package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.id.EmployeeId;
import id.perumdamts.mail.dto.id.UserId;

public class RecipientComponentDto {
    public record EmployeeInfoDto(UserId userId, EmployeeId empId, String empName, String posName) {
    }

    public record CirculationInfoDto(String type, String name) {
    }

    public record NotificationInfoDto(
            Integer emailNotif,
            Integer smsNotif,
            Boolean notified) {
    }
}
