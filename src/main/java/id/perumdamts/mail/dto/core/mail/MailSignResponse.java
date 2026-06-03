package id.perumdamts.mail.dto.core.mail;

public record MailSignResponse(
        String authCode,
        String qrUrl
) {
    public static MailSignResponse of(String authCode, String baseUrl) {
        String qrUrl = baseUrl + "/api/mails/verify-sign/" + authCode;
        return new MailSignResponse(authCode, qrUrl);
    }
}