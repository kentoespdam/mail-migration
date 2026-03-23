package id.perumdamts.mail.api.dto.recipient;

public record RecipientNotifPatchRequest(
        Integer emailNotif,
        Integer smsNotif
) {}
