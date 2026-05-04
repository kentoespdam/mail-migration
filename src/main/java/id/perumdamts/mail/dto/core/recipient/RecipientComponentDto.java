package id.perumdamts.mail.dto.core.recipient;

public class RecipientComponentDto {
    public record EmployeeInfoDto(String userId, String empId, String empName, String posName) {
    }

    public record CirculationInfoDto(String type, String name) {
    }

    public record NotificationInfoDto(
            Integer emailNotif,
            Integer smsNotif,
            Boolean notified) {
    }
}
