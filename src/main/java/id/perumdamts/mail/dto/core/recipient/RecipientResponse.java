package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.id.MailRecipientId;

public record RecipientResponse(
        MailRecipientId id,
        RecipientComponentDto.EmployeeInfoDto employee,
        RecipientComponentDto.CirculationInfoDto circulation,
        RecipientComponentDto.NotificationInfoDto notifications) {
}
