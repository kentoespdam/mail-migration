package id.perumdamts.mail.dto.core.recipient;

public record RecipientResponse(
        String id,
        RecipientComponentDto.EmployeeInfoDto employee,
        RecipientComponentDto.CirculationInfoDto circulation,
        RecipientComponentDto.NotificationInfoDto notifications) {
}
