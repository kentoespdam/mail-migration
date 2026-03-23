package id.perumdamts.mail.api.dto.recipient;

import id.perumdamts.mail.domain.entity.MailRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecipientMapper {

    @Mapping(target = "circulationName", expression = "java(entity.getCirculationType().name())")
    RecipientResponse toResponse(MailRecipient entity);
}
