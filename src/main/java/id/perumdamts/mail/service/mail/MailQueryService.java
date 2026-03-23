package id.perumdamts.mail.service.mail;

import id.perumdamts.mail.api.dto.mail.MailReportRequest;
import id.perumdamts.mail.api.dto.mail.MailReportResponse;
import id.perumdamts.mail.api.dto.mail.MailSearchRequest;
import id.perumdamts.mail.api.dto.mail.MailSummaryResponse;
import id.perumdamts.mail.repository.jooq.MailQueryRepository;
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

    public List<MailSummaryResponse> getThread(Integer mailId) {
        return mailQueryRepository.findThread(mailId);
    }

    public List<MailSummaryResponse> search(MailSearchRequest request) {
        return mailQueryRepository.searchMails(request);
    }

    public List<MailReportResponse> getReport(MailReportRequest request) {
        return mailQueryRepository.getReport(request);
    }
}
