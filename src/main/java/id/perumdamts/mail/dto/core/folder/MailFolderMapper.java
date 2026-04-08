package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper untuk konversi MailFolder entity ke DTO.
 * Menggunakan SQIDS untuk meng-encode ID.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MailFolderMapper extends SqidMapper<MailFolder> {

    @Autowired protected SqidsEncoder encoder;

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(target = "parentFolderId", expression = "java(entity.getParentFolderId() > 0 ? encoder.encode(MailFolder.class, entity.getParentFolderId()) : \"0\")")
    @Mapping(target = "ownerId", expression = "java(entity.getOwnerId() != null ? encoder.encode(MailFolder.class, entity.getOwnerId()) : \"0\")")
    @Mapping(target = "system", expression = "java(entity.isSystemFolder())")
    @Mapping(target = "unread", expression = "java(0L)")
    @Mapping(target = "total", expression = "java(0L)")
    @Mapping(source = "iconClsFolder", target = "iconCls")
    public abstract MailFolderResponse toResponse(MailFolder entity);
}
