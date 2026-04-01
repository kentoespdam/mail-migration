package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.dto.master.mailType.MailTypeMapper;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = MailTypeMapper.class)
public abstract class MailCategoryMapper extends SqidMapper<MailCategory> {

    @Autowired protected SqidsEncoder encoder;

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(source = "mailType", target = "mailType")
    @Mapping(source = "status", target = "status")
    public abstract MailCategoryResponse toResponse(MailCategory entity);
}
