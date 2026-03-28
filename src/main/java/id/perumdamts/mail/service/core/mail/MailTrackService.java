package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk pelacakan sirkulasi surat (trackMail / track_mail).
 * Menampilkan seluruh surat dalam satu thread berdasarkan m_root_id.
 * 
 * Migration notes:
 * - Content trimming → utility method
 * - Pertimbangkan caching untuk thread yang sering diakses
 */
@Service
@Transactional(readOnly = true)
public class MailTrackService {

    private final MailQueryRepository mailQueryRepository;
    private final MailThreadService mailThreadService;

    public MailTrackService(MailQueryRepository mailQueryRepository,
                            MailThreadService mailThreadService) {
        this.mailQueryRepository = mailQueryRepository;
        this.mailThreadService = mailThreadService;
    }

    /**
     * Track circulation surat - menampilkan semua mail dalam thread.
     * Equivalent dengan track_mail() di source PHP.
     * 
     * @param mailId ID mail (bisa root atau child)
     * @return flat list mails dalam thread (chronological)
     */
    public List<MailSummaryResponse> trackMail(Integer mailId) {
        return mailQueryRepository.findThread(mailId);
    }

    /**
     * Track circulation dengan tree structure.
     * Membangun thread tree dari flat data.
     * 
     * @param mailId ID mail (bisa root atau child)
     * @return tree structure dengan parent-child relationship
     */
    public List<MailThreadService.MailThreadNode> trackMailAsTree(Integer mailId) {
        List<MailSummaryResponse> flatData = mailQueryRepository.findThread(mailId);
        return mailThreadService.buildTree(flatData);
    }

    /**
     * Get preview content dengan trimming untuk display.
     * Utility method untuk trim content surat.
     * 
     * @param content content asli
     * @param maxLength max length preview
     * @return trimmed content dengan "..." jika dipotong
     */
    public String getPreviewContent(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        if (content.length() <= maxLength) {
            return content;
        }
        
        // Trim dan tambahkan "..." 
        String trimmed = content.substring(0, maxLength).trim();
        
        // Pastikan tidak cut di tengah kata
        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace > maxLength * 3 / 4) {
            trimmed = trimmed.substring(0, lastSpace);
        }
        
        return trimmed + "...";
    }
}
