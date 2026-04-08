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
    @Mapping(target = "userId", expression = "java(entity.getUserId() != null ? encoder.encode(MailRecipient.class, entity.getUserId()) : null)")
    @Mapping(target = "empId", expression = "java(entity.getEmpId() != null ? encoder.encode(MailRecipient.class, entity.getEmpId()) : null)")
    @Mapping(target = "circulation", expression = "java(String.valueOf(entity.getCirculation()))")
    @Mapping(target = "circulationName", expression = "java(entity.getCirculationType().name())")
    @Mapping(target = "notified", expression = "java(entity.isNotified())")
    @Mapping(target = "read", expression = "java(entity.isRead())")
    public abstract RecipientResponse toResponse(MailRecipient entity);
}
