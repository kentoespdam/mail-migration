package id.perumdamts.mail.dto.core.attachment;

import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public abstract class AttachmentMapper {

    @Autowired
    protected SqidsEncoder encoder;

    @Mapping(target = "id", expression = "java(mapAttachmentId(entity))")
    @Mapping(target = "refType", expression = "java(entity.getRefType() != null ? entity.getRefType().getDbValue() : null)")
    @Mapping(target = "refId", expression = "java(mapRefId(entity))")
    public abstract AttachmentResponse toResponse(Attachment entity);

    @Mapping(target = "id", expression = "java(mapAttachmentId(entity))")
    public abstract AttachmentDetailResponse toDetailResponse(Attachment entity);

    @Named("mapAttachmentId")
    protected String mapAttachmentId(Attachment entity) {
        if (entity.getId() == null)
            return null;
        return encoder.encode(Attachment.class, entity.getId().longValue());
    }

    @Named("mapRefId")
    protected String mapRefId(Attachment entity) {
        if (entity.getRefId() == null)
            return null;
        AttachmentRefType type = entity.getRefType(); // null-safe guard
        if (type == AttachmentRefType.MAIL) {
            return encoder.encode(Mail.class, entity.getRefId());
        }
        return String.valueOf(entity.getRefId());
    }
}
