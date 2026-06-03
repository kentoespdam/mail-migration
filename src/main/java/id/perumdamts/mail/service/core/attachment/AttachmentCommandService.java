package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.repository.core.jpa.AttachmentRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentCommandService {

    private final AttachmentRepository attachmentRepository;
    private final MailRepository mailRepository;
    private final AttachmentFileStorageService storageService;
    private final UserTaskQueryService userTaskQueryService;

    @Transactional
    public Attachment uploadAttachment(MultipartFile file, Long mailId, String docNotes, MailPrincipal principal) {
        validateMailAccess(mailId, principal);

        AttachmentFileStorageService.StoredFile storedFile = storageService.store(file);

        String originalFilename = storedFile.originalFileName();
        String fileExt = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0) {
            fileExt = originalFilename.substring(dot + 1).toLowerCase();
        }

        Attachment attachment = new Attachment(
                AttachmentRefType.MAIL,
                mailId,
                originalFilename,
                storedFile.systemFileName(),
                fileExt,
                (int) storedFile.fileSize(),
                principal.name()
        );
        attachment.setDocNotes(docNotes);

        return attachmentRepository.save(attachment);
    }

    @Transactional
    public void deleteAttachment(Integer attachmentId, Long mailId, MailPrincipal principal) {
        validateMailAccess(mailId, principal);

        Attachment attachment = attachmentRepository.findByIdAndRefIdAndRefType(
                        attachmentId, mailId, AttachmentRefType.MAIL.getDbValue())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        attachment.markDeleted();
        attachmentRepository.save(attachment);

        // Optionally delete file from storage? 
        // The plan says "Soft delete melalui RecordStatus", so we keep the file for now.
    }

    private void validateMailAccess(Long mailId, MailPrincipal principal) {
        // If mail doesn't exist, throw exception
        if (!mailRepository.existsById(mailId)) {
            throw new EntityNotFoundException("Mail not found: " + mailId);
        }

        // Validate user has access to this mail via UserTask
        userTaskQueryService.findUserTask(principal.userIdLong(), mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found in your mailbox: " + mailId));
    }
}
