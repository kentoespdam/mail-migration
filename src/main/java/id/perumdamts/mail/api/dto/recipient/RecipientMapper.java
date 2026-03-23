package id.perumdamts.mail.api.dto.recipient;

import id.perumdamts.mail.domain.entity.MailRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecipientMapper {

    @Mapping(target = "circulationName", expression = "java(entity.getCirculationType().name())")
    @Mapping(target = "notified", expression = "java(entity.isNotified())")
    @Mapping(target = "read", expression = "java(entity.isRead())")
    RecipientResponse toResponse(MailRecipient entity);
}
