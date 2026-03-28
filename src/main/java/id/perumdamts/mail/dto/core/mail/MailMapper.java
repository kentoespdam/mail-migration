package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.entity.core.Mail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailMapper {

    @Mapping(source = "mailType.id", target = "mailTypeId")
    @Mapping(source = "mailType.name", target = "mailTypeName")
    @Mapping(source = "mailCategory.id", target = "mailCategoryId")
    @Mapping(source = "mailCategory.name", target = "mailCategoryName")
    @Mapping(source = "rootMail.id", target = "rootMailId")
    @Mapping(source = "parentMail.id", target = "parentMailId")
    MailResponse toResponse(Mail entity);
}
