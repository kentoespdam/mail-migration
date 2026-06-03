package id.perumdamts.mail.dto.id;

public sealed interface SqidId permits
        MailId, MailTypeId, MailCategoryId, AttachmentId, PublicationId,
        DocumentTypeId, AllowedFileTypeId,
        QuickMessageId, MessageTemplateId, EmployeeId, OfficeId, PositionId, UserId,
        MailFolderId, MailRecipientId, CirculationTypeId {

    long value();

    Class<?> getEntityClass();
}
