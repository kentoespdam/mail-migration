package id.perumdamts.mail.api.dto.master;

import id.perumdamts.mail.domain.entity.QuickMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuickMessageMapper {

    QuickMessageResponse toResponse(QuickMessage entity);
}
