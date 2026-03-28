package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.entity.master.MailCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailCategoryMapper {

    @Mapping(source = "mailType.id", target = "mailTypeId")
    @Mapping(source = "mailType.name", target = "mailTypeName")
    @Mapping(source = "status", target = "status")
    MailCategoryResponse toResponse(MailCategory entity);
}
