package id.perumdamts.mail.api.dto.master;

import id.perumdamts.mail.domain.entity.MailType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailTypeMapper {

    @Mapping(target = "categoryCount", expression = "java(entity.getCategories() != null ? entity.getCategories().size() : 0)")
    MailTypeResponse toResponse(MailType entity);

    MailTypeLookup toLookup(MailType entity);
}
