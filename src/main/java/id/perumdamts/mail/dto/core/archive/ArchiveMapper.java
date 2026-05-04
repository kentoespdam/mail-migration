package id.perumdamts.mail.dto.core.archive;

import id.perumdamts.mail.entity.core.MailArchive;
import id.perumdamts.mail.entity.core.MailArchiveAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ArchiveMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "location.rack", target = "rack")
    @Mapping(source = "location.shelf", target = "shelf")
    @Mapping(source = "location.box", target = "box")
    @Mapping(target = "year", expression = "java(entity.getArchiveDate() != null ? (short) entity.getArchiveDate().getYear() : null)")
    ArchiveResponse toResponse(MailArchive entity);

    ArchiveAccessResponse toAccessResponse(MailArchiveAccess entity);
}
