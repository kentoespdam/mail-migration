package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.PrintLog;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jpa.PrintLogRepository;
import id.perumdamts.mail.security.MailPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service untuk verifikasi cetak surat (signMe + checkSign).
 * 
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
    private final HttpServletRequest httpServletRequest;

    public MailSignatureService(PrintLogRepository printLogRepository,
                                 MailRepository mailRepository,
                                 HttpServletRequest httpServletRequest) {
        this.printLogRepository = printLogRepository;
        this.mailRepository = mailRepository;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Generate verification code saat user mencetak surat.
     * Equivalent dengan signMe() di source PHP.
     * 
     * @param mailId ID mail yang akan dicetak
     * @param principal user yang mencetak
     * @return auth code yang di-generate
     */
    public String signMail(Integer mailId, MailPrincipal principal) {
        // Verify mail exists
        Mail mail = getMailOrThrow(mailId);
        
        // Generate unique auth code (fix: gunakan UUID instead of uniqid())
        String authCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        
        // Get IP address dan user agent dari request
        String ipAddress = getClientIpAddress();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        
        // Create print log record
        PrintLog printLog = PrintLog.create(
                mailId,
                principal.name(),
                ipAddress,
                userAgent != null ? userAgent : "Unknown"
        );
        printLog.setAuthCode(authCode);
        
        printLogRepository.save(printLog);
        
        return authCode;
    }

    /**
     * Verifikasi keaslian dokumen cetak berdasarkan auth code.
     * Equivalent dengan checkSign() di source PHP.
     * 
     * @param authCode kode verifikasi dari dokumen cetak
     * @return hasil verifikasi
     */
    @Transactional(readOnly = true)
    public MailSignatureVerificationResponse verifySignature(String authCode) {
        if (authCode == null || authCode.isBlank()) {
            return MailSignatureVerificationResponse.invalid("Kode verifikasi kosong");
        }
        
        // Lookup print log by auth code
        PrintLog printLog = printLogRepository.findByAuthCode(authCode)
                .orElse(null);
        
        if (printLog == null) {
            return MailSignatureVerificationResponse.invalid("Kode verifikasi tidak ditemukan");
        }
        
        // Get mail details
        Mail mail = mailRepository.findById(printLog.getMailId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mail not found: " + printLog.getMailId()));
        
        return MailSignatureVerificationResponse.valid(
                printLog.getMailId(),
                mail.getMailNumber(),
                mail.getSubject(),
                printLog.getPrintDate(),
                printLog.getUsername(),
                printLog.getIpAddress()
        );
    }

    /**
     * Get riwayat cetak untuk mail tertentu.
     * 
     * @param mailId ID mail
     * @return list print logs
     */
    @Transactional(readOnly = true)
    public List<PrintLog> getPrintHistory(Integer mailId) {
        return printLogRepository.findByMailIdOrderByPrintDateDesc(mailId);
    }

    /**
     * Get client IP address dari request.
     * Handle proxy/load balancer scenarios.
     */
    private String getClientIpAddress() {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", 
                           "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        
        for (String header : headers) {
            String ip = httpServletRequest.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return httpServletRequest.getRemoteAddr();
    }

    private Mail getMailOrThrow(Integer mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
    }
}
