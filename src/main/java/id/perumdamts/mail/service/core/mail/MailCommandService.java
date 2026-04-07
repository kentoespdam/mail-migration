package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.dto.core.recipient.RecipientBatchRequest;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.enums.CirculationType;
import id.perumdamts.mail.integration.hr.BatchIdsRequest;
import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.EmployeeResponse;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
import id.perumdamts.mail.security.MailPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MailCommandService {

    private final MailRepository mailRepository;
    private final MailTypeRepository mailTypeRepository;
    private final MailCategoryRepository mailCategoryRepository;
    private final MailRecipientRepository recipientRepository;
    private final UserTaskRepository userTaskRepository;
    private final MailMapper mailMapper;
    private final MailSendService mailSendService;
    private final HrServiceClient hrServiceClient;

    public MailCommandService(MailRepository mailRepository,
                               MailTypeRepository mailTypeRepository,
                               MailCategoryRepository mailCategoryRepository,
                               MailRecipientRepository recipientRepository,
                               UserTaskRepository userTaskRepository,
                               MailMapper mailMapper,
                               MailSendService mailSendService,
                               HrServiceClient hrServiceClient) {
        this.mailRepository = mailRepository;
        this.mailTypeRepository = mailTypeRepository;
        this.mailCategoryRepository = mailCategoryRepository;
        this.recipientRepository = recipientRepository;
        this.userTaskRepository = userTaskRepository;
        this.mailMapper = mailMapper;
        this.mailSendService = mailSendService;
        this.hrServiceClient = hrServiceClient;
    }

    @Transactional
    public MailResponse createDraft(MailCreateRequest request, MailPrincipal principal) {
        var mail = createAndPersistMail(
                request.subject(),
                request.content(),
                request.note(),
                request.mailTypeId(),
                request.mailCategoryId(),
                request.mailDate(),
                request.maxResponseDate(),
                request.noSuratMasuk(),
                request.asalSuratMasuk(),
                request.tglSuratMasuk(),
                request.tujuanSuratKeluar(),
                request.penerimaSuratKeluar(),
                request.rootMailId(),
                request.parentMailId(),
                principal
        );

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
            mail.setMailType(mailTypeRepository.getReferenceById(request.mailTypeId().longValue()));
        }
        if (request.mailCategoryId() != null) {
            mail.setMailCategory(mailCategoryRepository.getReferenceById(request.mailCategoryId().longValue()));
        }
        if (request.noSuratMasuk() != null) mail.setNoSuratMasuk(request.noSuratMasuk());
        if (request.asalSuratMasuk() != null) mail.setAsalSuratMasuk(request.asalSuratMasuk());
        if (request.tglSuratMasuk() != null) mail.setTglSuratMasuk(request.tglSuratMasuk());
        if (request.tujuanSuratKeluar() != null) mail.setTujuanSuratKeluar(request.tujuanSuratKeluar());
        if (request.penerimaSuratKeluar() != null) mail.setPenerimaSuratKeluar(request.penerimaSuratKeluar());

        mail.setUpdatedDate(LocalDateTime.now());

        List<MailRecipient> recipients = recipientRepository.findByMailId(mailId);
        mail.setToStr(Mail.buildToStr(recipients));

        return mailMapper.toResponse(mailRepository.save(mail));
    }

    @Transactional
    public MailResponse send(Integer mailId, MailPrincipal principal) {
        Mail mail = mailSendService.send(mailId, principal);
        return mailMapper.toResponse(mail);
    }

    @Transactional
    public MailResponse sendMail(MailSendRequest request, MailPrincipal principal) {
        var mail = createAndPersistMail(
                request.subject(),
                request.content(),
                request.note(),
                request.mailTypeId(),
                request.mailCategoryId(),
                request.mailDate(),
                request.maxResponseDate(),
                request.noSuratMasuk(),
                request.asalSuratMasuk(),
                request.tglSuratMasuk(),
                request.tujuanSuratKeluar(),
                request.penerimaSuratKeluar(),
                request.rootMailId(),
                request.parentMailId(),
                principal
        );

        // Add recipients
        addRecipients(mail, request.recipients(), principal);

        // Send the mail
        return send(mail.getId(), principal);
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

    private Mail createAndPersistMail(String subject,
                                      String content,
                                      String note,
                                      Integer mailTypeId,
                                      Integer mailCategoryId,
                                      LocalDate mailDate,
                                      LocalDate maxResponseDate,
                                      String noSuratMasuk,
                                      String asalSuratMasuk,
                                      String tglSuratMasuk,
                                      String tujuanSuratKeluar,
                                      String penerimaSuratKeluar,
                                      Integer rootMailId,
                                      Integer parentMailId,
                                      MailPrincipal principal) {
        var mail = new Mail();
        mail.setSubject(subject);
        mail.setContent(content);
        mail.setNote(note);
        mail.setMailDate(mailDate);
        mail.setMaxResponseDate(maxResponseDate);
        if (mailTypeId != null) {
            mail.setMailType(mailTypeRepository.getReferenceById(mailTypeId.longValue()));
        }
        if (mailCategoryId != null) {
            mail.setMailCategory(mailCategoryRepository.getReferenceById(mailCategoryId.longValue()));
        }
        mail.setNoSuratMasuk(noSuratMasuk);
        mail.setAsalSuratMasuk(asalSuratMasuk);
        mail.setTglSuratMasuk(tglSuratMasuk);
        mail.setTujuanSuratKeluar(tujuanSuratKeluar);
        mail.setPenerimaSuratKeluar(penerimaSuratKeluar);

        mail.setCreatedBy(Integer.parseInt(principal.userId()));
        mail.setCreatedByName(principal.name());
        mail.setCreatedDate(LocalDateTime.now());

        if (rootMailId != null && rootMailId > 0) {
            mail.setRootMail(mailRepository.getReferenceById(rootMailId));
            if (parentMailId != null && parentMailId > 0) {
                mail.setParentMail(mailRepository.getReferenceById(parentMailId));
            }
        }

        mail = mailRepository.save(mail);

        if (mail.getRootMail() == null) {
            mail.setRootMail(mail);
            mail = mailRepository.save(mail);
        }

        return mail;
    }

    private void addRecipients(Mail mail, List<RecipientBatchRequest> recipientRequests, MailPrincipal principal) {
        Integer currentUserId = Integer.parseInt(principal.userId());
        List<MailRecipient> toSave = new ArrayList<>();

        for (RecipientBatchRequest batchRequest : recipientRequests) {
            CirculationType circulationType = CirculationType.fromDbValue(batchRequest.circulation());
            List<Long> empIdLongs = batchRequest.empIds().stream().map(Integer::longValue).toList();

            EmployeeResponse response = hrServiceClient.getBatchEmployees(
                    new BatchIdsRequest(empIdLongs));
            List<EmployeeDto> employees = response.getData();

            Map<Long, EmployeeDto> empMap = employees.stream()
                    .collect(Collectors.toMap(EmployeeDto::id, Function.identity()));

            for (Integer empId : batchRequest.empIds()) {
                EmployeeDto emp = empMap.get(empId.longValue());
                if (emp == null) {
                    continue; // Skip not found employees
                }

                Integer userId = emp.id().intValue();
                if (userId.equals(currentUserId)) {
                    continue; // Skip sender
                }

                // Check if recipient already exists
                if (recipientRepository.existsByMailIdAndUserId(mail.getId(), userId)) {
                    continue;
                }

                var recipient = createRecipientFromEmployee(mail, emp, circulationType);
                toSave.add(recipient);
            }
        }

        recipientRepository.saveAll(toSave);
    }

    private MailRecipient createRecipientFromEmployee(Mail mail, EmployeeDto emp, CirculationType circulationType) {
        var recipient = new MailRecipient(mail, emp.id().intValue(), emp.id().intValue(), circulationType);
        recipient.setEmpName(emp.nama());
        if (emp.jabatanId() != null) {
            recipient.setPosId(emp.jabatanId().intValue());
        }
        if (emp.jabatanNama() != null) {
            recipient.setPosName(emp.jabatanNama());
        }
        return recipient;
    }

    private Mail getMailOrThrow(Integer mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
    }
}
