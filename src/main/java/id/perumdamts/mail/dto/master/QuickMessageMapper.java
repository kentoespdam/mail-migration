package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.entity.master.QuickMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuickMessageMapper {

    QuickMessageResponse toResponse(QuickMessage entity);
}
