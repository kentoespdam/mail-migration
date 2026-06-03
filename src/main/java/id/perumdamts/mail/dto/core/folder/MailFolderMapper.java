package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.entity.core.MailFolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper untuk konversi MailFolder entity ke DTO.
 * Menggunakan typed wrappers untuk ID.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MailFolderMapper {

    @Mapping(target = "id", expression = "java(toFolderId(entity))")
    @Mapping(target = "parentFolderId", expression = "java(entity.getParentFolderId() != null ? new MailFolderId(entity.getParentFolderId()) : null)")
    @Mapping(target = "ownerId", expression = "java(entity.getOwnerId() != null ? new UserId(entity.getOwnerId()) : null)")
    @Mapping(target = "system", expression = "java(entity.isSystemFolder())")
    @Mapping(target = "unread", expression = "java(0L)")
    @Mapping(target = "total", expression = "java(0L)")
    @Mapping(source = "iconClsFolder", target = "iconCls")
    public abstract MailFolderResponse toResponse(MailFolder entity);

    protected MailFolderId toFolderId(MailFolder entity) {
        return entity != null ? new MailFolderId(entity.getId()) : null;
    }
}
