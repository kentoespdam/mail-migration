package id.perumdamts.mail.service.mail;

import id.perumdamts.mail.api.dto.mail.MailCreateRequest;
import id.perumdamts.mail.api.dto.mail.MailMapper;
import id.perumdamts.mail.api.dto.mail.MailResponse;
import id.perumdamts.mail.api.dto.mail.MailUpdateRequest;
import id.perumdamts.mail.domain.entity.Mail;
import id.perumdamts.mail.domain.entity.MailRecipient;
import id.perumdamts.mail.domain.entity.UserTask;
import id.perumdamts.mail.domain.enums.SystemFolder;
import id.perumdamts.mail.domain.event.MailSentEvent;
import id.perumdamts.mail.infrastructure.security.MailPrincipal;
import id.perumdamts.mail.repository.jpa.*;
import id.perumdamts.mail.service.mail.numbering.MailNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MailCommandService {

    private final MailRepository mailRepository;
    private final MailTypeRepository mailTypeRepository;
    private final MailCategoryRepository mailCategoryRepository;
    private final MailRecipientRepository recipientRepository;
    private final UserTaskRepository userTaskRepository;
    private final MailNumberGenerator mailNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final MailMapper mailMapper;

    public MailCommandService(MailRepository mailRepository,
                               MailTypeRepository mailTypeRepository,
                               MailCategoryRepository mailCategoryRepository,
                               MailRecipientRepository recipientRepository,
                               UserTaskRepository userTaskRepository,
                               MailNumberGenerator mailNumberGenerator,
                               ApplicationEventPublisher eventPublisher,
                               MailMapper mailMapper) {
        this.mailRepository = mailRepository;
        this.mailTypeRepository = mailTypeRepository;
        this.mailCategoryRepository = mailCategoryRepository;
        this.recipientRepository = recipientRepository;
        this.userTaskRepository = userTaskRepository;
        this.mailNumberGenerator = mailNumberGenerator;
        this.eventPublisher = eventPublisher;
        this.mailMapper = mailMapper;
    }

    @Transactional
    public MailResponse createDraft(MailCreateRequest request, MailPrincipal principal) {
        var mail = new Mail();
        applyFields(mail, request);
        mail.setCreatedBy(Integer.parseInt(principal.userId()));
        mail.setCreatedByName(principal.name());
        mail.setCreatedDate(LocalDateTime.now());

        // Threading
        if (request.rootMailId() != null && request.rootMailId() > 0) {
            mail.setRootMail(mailRepository.getReferenceById(request.rootMailId()));
            if (request.parentMailId() != null && request.parentMailId() > 0) {
                mail.setParentMail(mailRepository.getReferenceById(request.parentMailId()));
            }
        }

        mail = mailRepository.save(mail);

        // Self-reference root if new mail
        if (mail.getRootMail() == null) {
            mail.setRootMail(mail);
            mail = mailRepository.save(mail);
        }

        // Create draft UserTask for sender
        userTaskRepository.save(UserTask.draft(Integer.parseInt(principal.userId()), mail.getId()));

        return mailMapper.toResponse(mail);
    }

    @Transactional
    public MailResponse updateDraft(Integer mailId, MailUpdateRequest request, MailPrincipal principal) {
        var mail = getMailOrThrow(mailId);
        if (!mail.isDraft()) {
            throw new IllegalStateException("Cannot update a sent mail");
        }

        if (request.subject() != null) mail.setSubject(request.subject());
        if (request.content() != null) mail.setContent(request.content());
        if (request.note() != null) mail.setNote(request.note());
        if (request.mailDate() != null) mail.setMailDate(request.mailDate());
        if (request.maxResponseDate() != null) mail.setMaxResponseDate(request.maxResponseDate());
        if (request.mailTypeId() != null) {
            mail.setMailType(mailTypeRepository.getReferenceById(request.mailTypeId()));
        }
        if (request.mailCategoryId() != null) {
            mail.setMailCategory(mailCategoryRepository.getReferenceById(request.mailCategoryId()));
        }
        if (request.noSuratMasuk() != null) mail.setNoSuratMasuk(request.noSuratMasuk());
        if (request.asalSuratMasuk() != null) mail.setAsalSuratMasuk(request.asalSuratMasuk());
        if (request.tglSuratMasuk() != null) mail.setTglSuratMasuk(request.tglSuratMasuk());
        if (request.tujuanSuratKeluar() != null) mail.setTujuanSuratKeluar(request.tujuanSuratKeluar());
        if (request.penerimaSuratKeluar() != null) mail.setPenerimaSuratKeluar(request.penerimaSuratKeluar());

        mail.setUpdatedDate(LocalDateTime.now());

        return mailMapper.toResponse(mailRepository.save(mail));
    }

    @Transactional
    public MailResponse send(Integer mailId, MailPrincipal principal) {
        var mail = getMailOrThrow(mailId);
        if (!mail.isDraft()) {
            throw new IllegalStateException("Mail already sent");
        }

        List<MailRecipient> recipients = recipientRepository.findByMailId(mailId);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Cannot send mail without recipients");
        }

        // Generate mail number
        String mailNumber = mailNumberGenerator.generate(mail);
        mail.send(mailNumber);

        // Build toStr
        String toStr = recipients.stream()
                .map(r -> r.getEmpName() != null ? r.getEmpName() : "User#" + r.getUserId())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        mail.setToStr(toStr);

        mailRepository.save(mail);

        // Create inbox UserTask per recipient
        Integer senderId = Integer.parseInt(principal.userId());
        List<UserTask> inboxTasks = recipients.stream()
                .map(r -> UserTask.inbox(r.getUserId(), mailId))
                .toList();
        userTaskRepository.saveAll(inboxTasks);

        // Move sender's task from DRAFT to SENT
        userTaskRepository.updateFolder(senderId, mailId,
                SystemFolder.DRAFT.getId(), SystemFolder.SENT.getId());

        // If reply: move parent to READ for sender
        if (mail.getParentMail() != null) {
            userTaskRepository.updateFolder(senderId, mail.getParentMail().getId(),
                    SystemFolder.INBOX.getId(), SystemFolder.READ.getId());
        }

        // Publish domain event
        List<Integer> recipientUserIds = recipients.stream()
                .map(MailRecipient::getUserId)
                .toList();
        eventPublisher.publishEvent(new MailSentEvent(
                mailId, senderId, principal.name(), recipientUserIds));

        return mailMapper.toResponse(mail);
    }

    @Transactional
    public void deleteMail(Integer mailId, MailPrincipal principal) {
        Integer userId = Integer.parseInt(principal.userId());
        var task = userTaskRepository.findActiveByUserIdAndMailId(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found in your mailbox: " + mailId));

        if (task.isInTrash()) {
            task.purge();
        } else {
            task.softDelete();
        }
        userTaskRepository.save(task);
    }

    @Transactional
    public void restoreMail(Integer mailId, MailPrincipal principal) {
        Integer userId = Integer.parseInt(principal.userId());
        var task = userTaskRepository.findByUserIdAndMailId(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
        if (!task.isInTrash()) {
            throw new IllegalStateException("Mail is not in trash");
        }
        task.restore();
        userTaskRepository.save(task);
    }

    @Transactional
    public void markRead(Integer mailId, MailPrincipal principal) {
        Integer userId = Integer.parseInt(principal.userId());
        var task = userTaskRepository.findActiveByUserIdAndMailId(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
        task.markRead();
        userTaskRepository.save(task);
    }

    private void applyFields(Mail mail, MailCreateRequest request) {
        mail.setSubject(request.subject());
        mail.setContent(request.content());
        mail.setNote(request.note());
        mail.setMailDate(request.mailDate());
        mail.setMaxResponseDate(request.maxResponseDate());
        if (request.mailTypeId() != null) {
            mail.setMailType(mailTypeRepository.getReferenceById(request.mailTypeId()));
        }
        if (request.mailCategoryId() != null) {
            mail.setMailCategory(mailCategoryRepository.getReferenceById(request.mailCategoryId()));
        }
        mail.setNoSuratMasuk(request.noSuratMasuk());
        mail.setAsalSuratMasuk(request.asalSuratMasuk());
        mail.setTglSuratMasuk(request.tglSuratMasuk());
        mail.setTujuanSuratKeluar(request.tujuanSuratKeluar());
        mail.setPenerimaSuratKeluar(request.penerimaSuratKeluar());
    }

    private Mail getMailOrThrow(Integer mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
    }
}
