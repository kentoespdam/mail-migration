package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.entity.master.MailType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MailTypeMapper extends SqidMapper<MailType> {

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(target = "categoryCount", expression = "java(entity.getCategories() != null ? entity.getCategories().size() : 0)")
    public abstract MailTypeResponse toResponse(MailType entity);

    public abstract MailTypeLookup toLookup(MailType entity);
}
