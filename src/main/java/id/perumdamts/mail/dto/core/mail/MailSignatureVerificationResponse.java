package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDateTime;

/**
 * Response untuk verifikasi cetak surat.
 */
public record MailSignatureVerificationResponse(
                boolean valid,
                String mailId,
                String mailNumber,
                LocalDateTime signedAt,
                String signerName,
                String signerPosition,
                String archiveStatus,
                String message) {

        /**
         * Create response untuk signature yang valid.
         */
        public static MailSignatureVerificationResponse valid(
                        String mailId, String mailNumber,
                        LocalDateTime signedAt, String signerName, String signerPosition,
                        String archiveStatus) {
                return new MailSignatureVerificationResponse(
                                true, mailId, mailNumber, signedAt, signerName, signerPosition, archiveStatus,
                                "Dokumen valid - tercatat dalam sistem");
        }

        /**
         * Create response untuk signature yang tidak valid.
         */
        public static MailSignatureVerificationResponse invalid(String message) {
                return new MailSignatureVerificationResponse(
                                false, null, null, null, null, null, null,
                                message != null ? message : "INVALID_OR_DELETED");
        }
}
