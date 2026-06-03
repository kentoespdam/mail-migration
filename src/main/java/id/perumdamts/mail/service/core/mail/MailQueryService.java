package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.repository.core.jooq.AttachmentQueryRepository;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailQueryService {

    private final MailQueryRepository mailQueryRepository;
    private final AttachmentQueryRepository attachmentQueryRepository;
    private final UserTaskQueryService userTaskQueryService;

    public MailResponse getDetail(Long mailId, Long userId) {
        MailResponse mail = mailQueryRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));

        if (!userTaskQueryService.existsActive(userId, mailId)) {
            throw new EntityNotFoundException("Mail not found or access denied: " + mailId);
        }

        var attachments = attachmentQueryRepository.findByRef(AttachmentRefType.MAIL, mailId);
        return new MailResponse(
                mail.id(),
                mail.mailNumber(),
                mail.mailDate(),
                mail.type(),
                mail.category(),
                mail.subject(),
                mail.content(),
                mail.note(),
                mail.maxResponseDate(),
                mail.status(),
                mail.thread(),
                mail.summary(),
                mail.audit(),
                mail.noSuratMasuk(),
                mail.asalSuratMasuk(),
                mail.tglSuratMasuk(),
                mail.tujuanSuratKeluar(),
                mail.penerimaSuratKeluar(),
                attachments
        );
    }

    public Page<MailLookupResponse> findLookup(Long userId, MailLookupParams params) {
        return userTaskQueryService.findAll(userId, params);
    }

    public List<MailSummaryResponse> findThread(Long mailId) {
        Long rootId = mailQueryRepository.resolveRootId(mailId);
        return findThreadByRootId(rootId);
    }

    @Cacheable(value = CacheConfig.CacheNames.MAIL_THREAD, key = "#rootId")
    public List<MailSummaryResponse> findThreadByRootId(Long rootId) {
        return mailQueryRepository.findThreadByRootId(rootId);
    }

    public Page<MailSummaryResponse> search(MailSearchRequest request) {
        List<MailSummaryResponse> items = mailQueryRepository.searchMails(request);
        long total = items.isEmpty() ? 0 : items.getFirst().totalCount();
        return new PageImpl<>(items, PageRequest.of(request.getPage(), request.getSize()), total);
    }

    public Page<MailReportResponse> findReport(MailReportRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            LocalDate now = LocalDate.now();
            request.setStartDate(now.with(TemporalAdjusters.firstDayOfMonth()));
            request.setEndDate(now.with(TemporalAdjusters.lastDayOfMonth()));
        }
        List<MailReportResponse> items = mailQueryRepository.getReport(request);
        long total = items.isEmpty() ? 0 : items.getFirst().totalCount();
        return new PageImpl<>(items, PageRequest.of(request.getPage(), request.getSize()), total);
    }
}
