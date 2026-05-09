package id.perumdamts.mail.dto.id;

import com.fasterxml.jackson.annotation.JsonValue;

public sealed interface SqidId permits
        MailId, MailTypeId, MailCategoryId, AttachmentId, PublicationId,
        QuickMessageId, MessageTemplateId, EmployeeId, OfficeId, PositionId, UserId {

    long value();

    Class<?> getEntityClass();
}
