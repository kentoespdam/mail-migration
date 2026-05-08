package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.mail.MailSignRequest;
import id.perumdamts.mail.dto.core.mail.MailSignResponse;
import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailSignatureService;
import id.perumdamts.mail.service.security.RateLimitService;
import id.perumdamts.mail.util.SqidsEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mail Signature", description = "API untuk tanda tangan dan verifikasi cetak surat")
public class MailSignatureController {

    private final MailSignatureService signatureService;
    private final RateLimitService rateLimitService;
    private final SqidsEncoder encoder;

    @Operation(summary = "Generate verification code untuk cetak surat")
    @PostMapping("/api/mails/{id}/sign")
    @PreAuthorize("hasAuthority('SCOPE_mail:write')")
    public MailSignResponse signMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody MailSignRequest request) {
        long mailId = encoder.decode(Mail.class, id);
        return signatureService.signMail(mailId, request.signerPosId());
    }

    @Operation(summary = "Verifikasi keaslian dokumen cetak (Public)")
    @GetMapping("/api/mails/verify-sign/{authCode}")
    public MailSignatureVerificationResponse verifySignature(
            @PathVariable String authCode,
            HttpServletRequest request) {
        String clientIp = getClientIp(request);
        if (!rateLimitService.resolveBucket(clientIp).tryConsume(1)) {
            return MailSignatureVerificationResponse.invalid("Too many requests. Please try again later.");
        }
        return signatureService.verifySignature(authCode, clientIp);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
