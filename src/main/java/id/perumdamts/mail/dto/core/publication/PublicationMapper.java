package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.entity.master.AllowedFileType;
import id.perumdamts.mail.entity.master.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PublicationMapper {

    @Mapping(source = "documentType.id", target = "documentTypeId")
    @Mapping(source = "documentType.name", target = "documentTypeName")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "totalCount", ignore = true)
    PublicationDto toDto(Publication entity);

    DocumentTypeDto toDto(DocumentType entity);

    AllowedFileTypeDto toDto(AllowedFileType entity);
}
