package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import id.perumdamts.mail.repository.core.jpa.AttachmentRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailQueryService {

    private final MailQueryRepository mailQueryRepository;
    private final MailRepository mailRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserTaskQueryService userTaskQueryService;
    private final MailMapper mailMapper;

    public MailResponse getDetail(Long mailId) {
        var mail = mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
        var attachments = attachmentRepository.findByRefTypeAndRefId(
                AttachmentRefType.MAIL.getDbValue(), mailId);
        return mailMapper.toResponse(mail, attachments);
    }

    public Page<MailLookupResponse> lookup(Long userId, MailLookupParams params) {
        return userTaskQueryService.findAll(userId, params);
    }

    public List<MailTrackingItemResponse> findThreadTracking(Long mailId) {
        Long rootId = userTaskQueryService.resolveRootId(mailId);
        return userTaskQueryService.findThread(rootId);
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
