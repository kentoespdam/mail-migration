package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RecipientMapper extends SqidMapper<MailRecipient> {

    @Autowired
    protected SqidsEncoder encoder;

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(target = "employee", expression = "java(toEmployeeDto(entity))")
    @Mapping(target = "circulation", expression = "java(toCirculationDto(entity))")
    @Mapping(target = "notifications", expression = "java(toNotificationDto(entity))")
    public abstract RecipientResponse toResponse(MailRecipient entity);

    protected RecipientComponentDto.EmployeeInfoDto toEmployeeDto(MailRecipient entity) {
        return new RecipientComponentDto.EmployeeInfoDto(
                entity.getUserId() != null ? encoder.encode(MailRecipient.class, entity.getUserId()) : null,
                entity.getEmpId() != null ? encoder.encode(MailRecipient.class, entity.getEmpId()) : null,
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
                entity.isNotified(),
                entity.isRead(),
                entity.getFolderPosition());
    }
}
