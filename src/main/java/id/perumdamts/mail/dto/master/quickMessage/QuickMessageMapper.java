package id.perumdamts.mail.dto.master.quickMessage;

import id.perumdamts.mail.entity.master.QuickMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = id.perumdamts.mail.dto.id.QuickMessageId.class)
public interface QuickMessageMapper {

    @Mapping(target = "id", expression = "java(new QuickMessageId(entity.getId()))")
    QuickMessageResponse toResponse(QuickMessage entity);
}
