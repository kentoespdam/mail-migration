package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.attachment.AttachmentDetailResponse;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.repository.core.jpa.AttachmentRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jooq.AttachmentQueryRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentQueryService {

    private final AttachmentQueryRepository queryRepository;
    private final AttachmentRepository attachmentRepository;
    private final MailRepository mailRepository;
    private final AttachmentFileStorageService storageService;
    private final UserTaskQueryService userTaskQueryService;
    private final id.perumdamts.mail.repository.core.jpa.AttachmentDownloadHistoryRepository historyRepository;
    private final SqidsEncoder encoder;

    public List<AttachmentResponse> getAttachmentsByMailId(Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);
        return queryRepository.findByRef(AttachmentRefType.MAIL, mailId);
    }

    @Cacheable(value = CacheConfig.CacheNames.ATTACHMENTS, key = "'detail:' + #principal.userId() + ':' + #attachmentId + ':v1'")
    public AttachmentDetailResponse getAttachmentDetail(Integer attachmentId, Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);

        Attachment attachment = attachmentRepository.findByIdAndRefIdAndRefType(
                attachmentId, mailId, AttachmentRefType.MAIL.getDbValue())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        return new AttachmentDetailResponse(
                attachment.getId() != null ? encoder.encode(Attachment.class, attachment.getId().longValue()) : null,
                attachment.getOriginalFilename(),
                attachment.getFileExt(),
                attachment.getFileSize(),
                attachment.getDocNotes(),
                attachment.getUploadDate(),
                attachment.getUploadByName()
        );
    }

    @Transactional
    public Resource downloadAttachment(Integer attachmentId, Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);

        Attachment attachment = attachmentRepository.findByIdAndRefIdAndRefType(
                attachmentId, mailId, AttachmentRefType.MAIL.getDbValue())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        // Log download history
        historyRepository.save(new id.perumdamts.mail.entity.core.AttachmentDownloadHistory(
                attachment.getId(),
                principal.userIdLong().intValue(),
                principal.name(),
                null));

        return storageService.load(attachment.getSystemFilename(), attachment.getUploadDate());
    }

    private void validateMailAccess(Long mailId, MailPrincipal principal) {
        if (!mailRepository.existsById(mailId)) {
            throw new EntityNotFoundException("Mail not found: " + mailId);
        }

        userTaskQueryService.findUserTask(principal.userIdLong(), mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found in your mailbox: " + mailId));
    }
}
