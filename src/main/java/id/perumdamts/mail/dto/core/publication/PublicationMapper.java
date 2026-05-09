package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.master.allowedFileType.AllowedFileTypeDto;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.entity.master.AllowedFileType;
import id.perumdamts.mail.entity.master.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PublicationMapper {

    @Mapping(target = "id", expression = "java(new PublicationId(entity.getId()))")
    @Mapping(source = "documentType", target = "documentType")
    @Mapping(source = "status", target = "status")
    public abstract PublicationResponse toDto(Publication entity);

    @Mapping(target = "id", expression = "java(entity.getId() != null ? new DocumentTypeId(entity.getId()) : null)")
    public abstract DocumentTypeLookup toDto(DocumentType entity);

    @Mapping(target = "id", expression = "java(new AllowedFileTypeId(entity.getId()))")
    public abstract AllowedFileTypeDto toDto(AllowedFileType entity);
}
