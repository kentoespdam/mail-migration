package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailSignatureService;
import id.perumdamts.mail.util.SqidsEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mail Signature", description = "API untuk tanda tangan dan verifikasi cetak surat")
public class MailSignatureController {

    private final MailSignatureService signatureService;
    private final SqidsEncoder encoder;

    @Operation(summary = "Generate verification code untuk cetak surat")
    @PostMapping("/api/v1/mails/{id}/sign")
    public String signMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        return signatureService.signMail(mailId, principal);
    }

    @Operation(summary = "Verifikasi keaslian dokumen cetak (Public)")
    @GetMapping("/api/mails/verify-sign/{authCode}")
    public MailSignatureVerificationResponse verifySignature(@PathVariable String authCode) {
        return signatureService.verifySignature(authCode);
    }
}
