package id.perumdamts.mail.api.dto.folder;

import id.perumdamts.mail.domain.entity.PersonalFolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailFolderMapper {

    @Mapping(target = "system", expression = "java(entity.isSystemFolder())")
    @Mapping(target = "unread", expression = "java(0L)")
    @Mapping(target = "total", expression = "java(0L)")
    @Mapping(source = "iconClass", target = "iconClass")
    MailFolderResponse toResponse(PersonalFolder entity);
}
