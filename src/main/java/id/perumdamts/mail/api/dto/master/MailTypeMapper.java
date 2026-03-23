package id.perumdamts.mail.api.dto.master;

import id.perumdamts.mail.domain.entity.MailType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailTypeMapper {

    MailTypeResponse toResponse(MailType entity);
}
