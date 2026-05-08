package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailSignResponse;
import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailArchive;
import id.perumdamts.mail.entity.core.PrintLog;
import id.perumdamts.mail.repository.core.jpa.MailArchiveRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jpa.PrintLogRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service untuk verifikasi cetak surat (signMe + checkSign).
 * Migration notes dari source PHP:
 * - Ganti uniqid() dengan UUID.randomUUID()
 * - checkSign() return JSON (bukan HTML)
 * - Pertimbangkan QR code yang berisi URL verifikasi
 */
@Service
@Transactional
public class MailSignatureService {

    private final PrintLogRepository printLogRepository;
    private final MailRepository mailRepository;
    private final MailArchiveRepository mailArchiveRepository;
    private final HttpServletRequest httpServletRequest;
    private final SqidsEncoder encoder;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public MailSignatureService(PrintLogRepository printLogRepository,
            MailRepository mailRepository,
            MailArchiveRepository mailArchiveRepository,
            HttpServletRequest httpServletRequest,
            SqidsEncoder encoder) {
        this.printLogRepository = printLogRepository;
        this.mailRepository = mailRepository;
        this.mailArchiveRepository = mailArchiveRepository;
        this.httpServletRequest = httpServletRequest;
        this.encoder = encoder;
    }

    /**
     * Generate verification code saat user mencetak surat.
     * Equivalent dengan signMe() di source PHP.
     *
     * @param mailId     ID mail yang akan dicetak
     * @param signerPosId posisi ID yang menandatangani
     * @return response dengan authCode dan qrUrl
     */
    public MailSignResponse signMail(Long mailId, Long signerPosId) {
        Mail mail = mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));

        if (Boolean.TRUE.equals(mail.getDeleted())) {
            throw new IllegalStateException("Cannot sign a deleted mail");
        }

        String authCode = generateUniqueAuthCode();

        String ipAddress = getClientIpAddress();

        PrintLog printLog = PrintLog.create(
                mailId,
                "PosId:" + signerPosId,
                ipAddress);
        printLog.setAuthCode(authCode);

        printLogRepository.save(printLog);

        return MailSignResponse.of(authCode, baseUrl);
    }

    private String generateUniqueAuthCode() {
        String authCode;
        int attempts = 0;
        do {
            authCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            attempts++;
            if (attempts > 10) {
                throw new IllegalStateException("Failed to generate unique auth code");
            }
        } while (printLogRepository.existsByAuthCode(authCode));
        return authCode;
    }

    /**
     * Verifikasi keaslian dokumen cetak berdasarkan auth code.
     * Equivalent dengan checkSign() di source PHP.
     *
     * @param authCode kode verifikasi dari dokumen cetak
     * @param clientIp IP address of the verifier
     * @return hasil verifikasi
     */
    @Transactional
    public MailSignatureVerificationResponse verifySignature(String authCode, String clientIp) {
        if (authCode == null || authCode.isBlank()) {
            return MailSignatureVerificationResponse.invalid("INVALID_OR_DELETED");
        }

        PrintLog printLog = printLogRepository.findByAuthCode(authCode)
                .orElse(null);

        if (printLog == null) {
            if (authCode.length() == 13) {
                return MailSignatureVerificationResponse.invalid("INVALID_OR_DELETED");
            }
            return MailSignatureVerificationResponse.invalid("INVALID_OR_DELETED");
        }

        Mail mail = mailRepository.findById(printLog.getMailId())
                .orElse(null);

        if (mail == null || Boolean.TRUE.equals(mail.getDeleted())) {
            return MailSignatureVerificationResponse.invalid("INVALID_OR_DELETED");
        }

        printLog.recordVerification(clientIp);
        printLogRepository.save(printLog);

        String signerName = printLog.getUsername();
        String signerPosition = extractSignerPosition(signerName);
        String archiveStatus = getArchiveStatus(printLog.getMailId());

        return MailSignatureVerificationResponse.valid(
                encoder.encode(Mail.class, printLog.getMailId()),
                mail.getMailNumber(),
                printLog.getPrintDate(),
                signerName,
                signerPosition,
                archiveStatus);
    }

    private String extractSignerPosition(String username) {
        if (username != null && username.startsWith("PosId:")) {
            return username.substring(6);
        }
        return null;
    }

    private String getArchiveStatus(Long mailId) {
        Optional<MailArchive> archive = mailArchiveRepository.findActiveById(mailId);
        return archive.map(a -> a.getArchiveStatus() != null ? a.getArchiveStatus().name() : null).orElse(null);
    }

    /**
     * Get riwayat cetak untuk mail tertentu.
     * 
     * @param mailId ID mail
     * @return list print logs
     */
    @Transactional(readOnly = true)
    public List<PrintLog> getPrintHistory(Long mailId) {
        return printLogRepository.findByMailIdOrderByPrintDateDesc(mailId);
    }

    /**
     * Get client IP address dari request.
     * Handle proxy/load balancer scenarios.
     */
    private String getClientIpAddress() {
        String[] headers = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" };

        for (String header : headers) {
            String ip = httpServletRequest.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return httpServletRequest.getRemoteAddr();
    }

    private Mail getMailOrThrow(Long mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
    }
}
