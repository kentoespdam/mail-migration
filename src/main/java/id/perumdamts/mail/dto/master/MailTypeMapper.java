package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.entity.master.MailType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailTypeMapper {

    @Mapping(target = "categoryCount", expression = "java(entity.getCategories() != null ? entity.getCategories().size() : 0)")
    MailTypeResponse toResponse(MailType entity);

    MailTypeLookup toLookup(MailType entity);
}
