package id.perumdamts.mail.api.dto.publication;

import id.perumdamts.mail.domain.entity.AllowedFileType;
import id.perumdamts.mail.domain.entity.DocumentType;
import id.perumdamts.mail.domain.entity.Publication;
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
