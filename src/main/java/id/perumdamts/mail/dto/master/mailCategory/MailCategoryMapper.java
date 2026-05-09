package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.dto.id.MailCategoryId;
import id.perumdamts.mail.dto.master.mailType.MailTypeMapper;
import id.perumdamts.mail.entity.master.MailCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = MailTypeMapper.class)
public abstract class MailCategoryMapper extends SqidMapper<MailCategory> {

    @Mapping(target = "id", expression = "java(new MailCategoryId(entity.getId()))")
    @Mapping(source = "mailType", target = "mailType")
    @Mapping(source = "status", target = "status")
    public abstract MailCategoryResponse toResponse(MailCategory entity);

    @Mapping(target = "id", expression = "java(new MailCategoryId(entity.getId()))")
    public abstract MailCategoryLookup toLookup(MailCategory entity);
}
