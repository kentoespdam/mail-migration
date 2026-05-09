package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.dto.id.EmployeeId;
import id.perumdamts.mail.dto.id.MailRecipientId;
import id.perumdamts.mail.dto.id.UserId;
import id.perumdamts.mail.entity.core.MailRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RecipientMapper extends SqidMapper<MailRecipient> {

    @Mapping(target = "id", expression = "java(toRecipientId(entity))")
    @Mapping(target = "employee", expression = "java(toEmployeeDto(entity))")
    @Mapping(target = "circulation", expression = "java(toCirculationDto(entity))")
    @Mapping(target = "notifications", expression = "java(toNotificationDto(entity))")
    public abstract RecipientResponse toResponse(MailRecipient entity);

    protected MailRecipientId toRecipientId(MailRecipient entity) {
        return entity != null ? new MailRecipientId(entity.getId()) : null;
    }

    protected RecipientComponentDto.EmployeeInfoDto toEmployeeDto(MailRecipient entity) {
        return new RecipientComponentDto.EmployeeInfoDto(
                entity.getUserId() != null ? new UserId(entity.getUserId()) : null,
                entity.getEmpId() != null ? new EmployeeId(entity.getEmpId()) : null,
                entity.getEmpName(),
                entity.getPosName());
    }

    protected RecipientComponentDto.CirculationInfoDto toCirculationDto(MailRecipient entity) {
        return new RecipientComponentDto.CirculationInfoDto(
                String.valueOf(entity.getCirculation()),
                entity.getCirculationType().name());
    }

    protected RecipientComponentDto.NotificationInfoDto toNotificationDto(MailRecipient entity) {
        return new RecipientComponentDto.NotificationInfoDto(
                entity.getEmailNotif(),
                entity.getSmsNotif(),
                entity.isNotified());
    }
}
