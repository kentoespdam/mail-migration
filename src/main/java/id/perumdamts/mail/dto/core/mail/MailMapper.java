package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.SqidMapper;
import id.perumdamts.mail.dto.core.attachment.AttachmentMapper;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.util.SqidsEncoder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MailMapper extends SqidMapper<Mail> {

    @Autowired
    protected SqidsEncoder encoder;

    @Autowired
    protected AttachmentMapper attachmentMapper;

    @Mapping(target = "id", expression = "java(sqid(entity))")
    @Mapping(target = "type", expression = "java(toTypeDto(entity))")
    @Mapping(target = "category", expression = "java(toCategoryDto(entity))")
    @Mapping(target = "thread", expression = "java(toThreadDto(entity))")
    @Mapping(target = "audit", expression = "java(toAuditDto(entity))")
    @Mapping(target = "summary", expression = "java(toSummaryDto(entity))")
    @Mapping(target = "attachments", expression = "java(toAttachmentDtos(attachments))")
    public abstract MailResponse toResponse(Mail entity, List<Attachment> attachments);

    public abstract MailResponse toResponse(Mail entity);

    protected List<AttachmentResponse> toAttachmentDtos(List<Attachment> attachments) {
        if (attachments == null) return null;
        return attachments.stream().map(attachmentMapper::toResponse).toList();
    }

    protected MailTypeLookup toTypeDto(Mail entity) {
        return new MailTypeLookup(
                entity.getMailType() != null ? encoder.encode(MailType.class, entity.getMailType().getId()) : null,
                entity.getMailType() != null ? entity.getMailType().getName() : null);
    }

    protected MailCategoryLookup toCategoryDto(Mail entity) {
        return new MailCategoryLookup(
                entity.getMailCategory() != null ? encoder.encode(MailCategory.class, entity.getMailCategory().getId())
                        : null,
                entity.getMailCategory() != null ? entity.getMailCategory().getName() : null);
    }

    protected MailComponentDto.MailThreadInfoDto toThreadDto(Mail entity) {
        return new MailComponentDto.MailThreadInfoDto(
                entity.getRootMail() != null ? encoder.encode(Mail.class, entity.getRootMail().getId()) : null,
                entity.getParentMail() != null ? encoder.encode(Mail.class, entity.getParentMail().getId()) : null);
    }

    protected MailComponentDto.MailAuditInfoDto toAuditDto(Mail entity) {
        return new MailComponentDto.MailAuditInfoDto(
                entity.getCreatedBy() != null ? encoder.encode(Mail.class, entity.getCreatedBy()) : null,
                entity.getCreatedByName(),
                entity.getCreatedDate(),
                entity.getUpdatedDate());
    }

    protected MailComponentDto.MailSummaryInfoDto toSummaryDto(Mail entity) {
        return new MailComponentDto.MailSummaryInfoDto(
                entity.getAttachmentQty(),
                entity.getToStr());
    }
}
