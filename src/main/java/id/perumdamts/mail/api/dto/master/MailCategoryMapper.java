package id.perumdamts.mail.api.dto.master;

import id.perumdamts.mail.domain.entity.MailCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailCategoryMapper {

    @Mapping(source = "mailType.id", target = "mailTypeId")
    @Mapping(source = "mailType.name", target = "mailTypeName")
    @Mapping(source = "status.dbValue", target = "status")
    MailCategoryResponse toResponse(MailCategory entity);
}
