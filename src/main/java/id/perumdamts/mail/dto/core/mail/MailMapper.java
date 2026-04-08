package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MailMapper extends SqidMapper<Mail> {

    @Autowired
    protected SqidsEncoder encoder;

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(target = "mailTypeId", expression = "java(entity.getMailType() != null ? encoder.encode(MailType.class, entity.getMailType().getId()) : null)")
    @Mapping(source = "mailType.name", target = "mailTypeName")
    @Mapping(target = "mailCategoryId", expression = "java(entity.getMailCategory() != null ? encoder.encode(MailCategory.class, entity.getMailCategory().getId()) : null)")
    @Mapping(source = "mailCategory.name", target = "mailCategoryName")
    @Mapping(target = "rootMailId", expression = "java(entity.getRootMail() != null ? encoder.encode(Mail.class, entity.getRootMail().getId()) : null)")
    @Mapping(target = "parentMailId", expression = "java(entity.getParentMail() != null ? encoder.encode(Mail.class, entity.getParentMail().getId()) : null)")
    @Mapping(target = "createdBy", expression = "java(entity.getCreatedBy() != null ? encoder.encode(Mail.class, entity.getCreatedBy()) : null)")
    public abstract MailResponse toResponse(Mail entity);
}
