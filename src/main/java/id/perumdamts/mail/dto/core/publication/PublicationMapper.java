package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.master.allowedFileType.AllowedFileTypeDto;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeDto;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.entity.master.AllowedFileType;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PublicationMapper {

    @Autowired protected SqidsEncoder encoder;

    @Mapping(target = "id", expression = "java(entity.getId() != null ? encoder.encode(Publication.class, entity.getId()) : null)")
    @Mapping(source = "documentType", target = "documentType")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "totalCount", ignore = true)
    public abstract PublicationDto toDto(Publication entity);

    public abstract DocumentTypeDto toDto(DocumentType entity);

    @Mapping(target = "id", expression = "java(encoder.encode(AllowedFileType.class, entity.getId()))")
    public abstract AllowedFileTypeDto toDto(AllowedFileType entity);
}
