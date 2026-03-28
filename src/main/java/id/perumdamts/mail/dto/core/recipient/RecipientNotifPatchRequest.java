package id.perumdamts.mail.dto.core.recipient;

public record RecipientNotifPatchRequest(
        Integer emailNotif,
        Integer smsNotif
) {}
