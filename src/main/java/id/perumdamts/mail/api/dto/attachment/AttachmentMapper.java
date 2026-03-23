package id.perumdamts.mail.api.dto.attachment;

import id.perumdamts.mail.domain.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttachmentMapper {

    @Mapping(target = "refType", expression = "java(entity.getRefType().getDbValue())")
    AttachmentResponse toResponse(Attachment entity);
}
