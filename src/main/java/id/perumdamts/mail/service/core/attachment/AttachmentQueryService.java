package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.attachment.AttachmentDetailResponse;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.dto.id.AttachmentId;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.repository.core.jpa.AttachmentRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jooq.AttachmentQueryRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
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

    public List<AttachmentResponse> getAttachmentsByMailId(Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);
        return queryRepository.findByRef(AttachmentRefType.MAIL, mailId);
    }

    @Cacheable(value = CacheConfig.CacheNames.ATTACHMENTS, key = "'detail:' + #principal.userId() + ':' + #attachmentId + ':v1'")
    public AttachmentDetailResponse getAttachmentDetail(long attachmentId, Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);

        Attachment attachment = attachmentRepository.findByIdAndRefIdAndRefType(
                attachmentId, mailId, AttachmentRefType.MAIL.getDbValue())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        return new AttachmentDetailResponse(
                attachment.getId() != null ? new AttachmentId(attachment.getId()) : null,
                attachment.getOriginalFilename(),
                attachment.getFileExt(),
                attachment.getFileSize(),
                attachment.getDocNotes(),
                attachment.getUploadDate(),
                attachment.getUploadByName()
        );
    }

    @Transactional
    public Resource downloadAttachment(long attachmentId, Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);

        Attachment attachment = attachmentRepository.findByIdAndRefIdAndRefType(
                attachmentId, mailId, AttachmentRefType.MAIL.getDbValue())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        // Log download history
        historyRepository.save(new id.perumdamts.mail.entity.core.AttachmentDownloadHistory(
                attachment,
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

    public List<AttachmentResponse> findByOwner(AttachmentRefType refType, Long refId) {
        return queryRepository.findByRef(refType, refId);
    }

    public AttachmentResponse findById(long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + id));
    }

    @Transactional
    public Resource download(long attachmentId, MailPrincipal principal) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        historyRepository.save(new id.perumdamts.mail.entity.core.AttachmentDownloadHistory(
                attachment,
                principal.userIdLong().intValue(),
                principal.name(),
                null));

        return storageService.load(attachment.getSystemFilename(), attachment.getUploadDate());
    }
}
