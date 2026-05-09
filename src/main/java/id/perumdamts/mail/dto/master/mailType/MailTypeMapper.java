package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.entity.master.MailType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MailTypeMapper {

    @Mapping(target = "id", expression = "java(new MailTypeId(entity.getId()))")
    @Mapping(target = "categoryCount", expression = "java(entity.getCategories() != null ? entity.getCategories().size() : 0)")
    public abstract MailTypeResponse toResponse(MailType entity);

    @Mapping(target = "id", expression = "java(new MailTypeId(entity.getId()))")
    public abstract MailTypeLookup toLookup(MailType entity);

    @Mapping(target = "id", expression = "java(new MailTypeId(entity.getId()))")
    public abstract MailTypeMiniResponse toMiniResponse(MailType entity);
}
