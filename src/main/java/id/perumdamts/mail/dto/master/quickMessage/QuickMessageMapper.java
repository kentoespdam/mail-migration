package id.perumdamts.mail.dto.master.quickMessage;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.entity.master.QuickMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class QuickMessageMapper extends SqidMapper<QuickMessage> {

    @Mapping(target = "id", expression = "java(sqid(entity))")
    public abstract QuickMessageResponse toResponse(QuickMessage entity);
}
