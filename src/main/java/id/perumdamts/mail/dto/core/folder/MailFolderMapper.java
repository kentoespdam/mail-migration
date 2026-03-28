package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.entity.core.PersonalFolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailFolderMapper {

    @Mapping(target = "system", expression = "java(entity.isSystemFolder())")
    @Mapping(target = "unread", expression = "java(0L)")
    @Mapping(target = "total", expression = "java(0L)")
    @Mapping(source = "iconClsFolder", target = "iconCls")
    MailFolderResponse toResponse(PersonalFolder entity);
}
