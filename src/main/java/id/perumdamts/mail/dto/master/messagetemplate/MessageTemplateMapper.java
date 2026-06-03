package id.perumdamts.mail.dto.master.messagetemplate;

import id.perumdamts.mail.entity.master.MessageTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MessageTemplateMapper {

    @Mapping(target = "id", expression = "java(new MessageTemplateId(entity.getId()))")
    public abstract MessageTemplateResponse toResponse(MessageTemplate entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract MessageTemplate toEntity(MessageTemplateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget MessageTemplate entity, MessageTemplateRequest request);
}
