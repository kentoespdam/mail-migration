package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MailQueryService {

    private final MailQueryRepository mailQueryRepository;

    public MailQueryService(MailQueryRepository mailQueryRepository) {
        this.mailQueryRepository = mailQueryRepository;
    }

    public List<MailSummaryResponse> getThread(Long mailId) {
        return mailQueryRepository.findThread(mailId);
    }

    public PagedResponse<MailSummaryResponse> search(MailSearchRequest request) {
        List<MailSummaryResponse> items = mailQueryRepository.searchMails(request);
        long total = items.isEmpty() ? 0 : items.getFirst().getTotalCount();
        return PagedResponse.of(items, request, total);
    }

    public List<MailTrackingResponse> getTracking(Long mailId) {
        return mailQueryRepository.findTracking(mailId);
    }

    public List<RecipientReadStatusResponse> getReadStatus(Long mailId) {
        return mailQueryRepository.findReadStatus(mailId);
    }

    public PagedResponse<MailReportResponse> getReport(MailReportRequest request) {
        List<MailReportResponse> items = mailQueryRepository.getReport(request);
        long total = items.isEmpty() ? 0 : items.getFirst().getTotalCount();
        return PagedResponse.of(items, request, total);
    }
}
