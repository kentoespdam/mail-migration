package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.entity.master.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class DocumentTypeMapper extends SqidMapper<DocumentType> {

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(target = "publicationCount", expression = "java(entity.getPublications() != null ? entity.getPublications().size() : 0)")
    public abstract DocumentTypeResponse toResponse(DocumentType entity);

    @Mapping(target = "id", expression = "java(new id.perumdamts.mail.dto.id.DocumentTypeId(entity.getId()))")
    public abstract DocumentTypeLookup toLookup(DocumentType entity);
}
