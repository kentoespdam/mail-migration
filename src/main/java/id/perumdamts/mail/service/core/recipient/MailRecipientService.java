package id.perumdamts.mail.service.core.recipient;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.enums.CirculationType;
import id.perumdamts.mail.integration.hr.BatchIdsRequest;
import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.EmployeeResponse;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.repository.core.jooq.RecipientQueryRepository;
import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MailRecipientService {

    private final MailRecipientRepository recipientRepository;
    private final MailRepository mailRepository;
    private final HrServiceClient hrServiceClient;
    private final RecipientMapper recipientMapper;
    private final RecipientQueryRepository recipientQueryRepository;

    public MailRecipientService(MailRecipientRepository recipientRepository,
                                 MailRepository mailRepository,
                                 HrServiceClient hrServiceClient,
                                 RecipientMapper recipientMapper,
                                 RecipientQueryRepository recipientQueryRepository) {
        this.recipientRepository = recipientRepository;
        this.mailRepository = mailRepository;
        this.hrServiceClient = hrServiceClient;
        this.recipientMapper = recipientMapper;
        this.recipientQueryRepository = recipientQueryRepository;
    }

    public List<RecipientResponse> getRecipients(Integer mailId) {
        return recipientRepository.findByMailId(mailId).stream()
                .map(recipientMapper::toResponse)
                .toList();
    }

    @Transactional
    public RecipientResponse addRecipient(Integer mailId, RecipientRequest request, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        CirculationType circulationType = CirculationType.fromDbValue(request.circulation());

        EmployeeDto emp = hrServiceClient.getEmployee(request.empId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.empId()));

        Integer userId = emp.id().intValue();
        if (recipientRepository.existsByMailIdAndUserId(mailId, userId)) {
            throw new IllegalArgumentException("Recipient already exists for this mail");
        }

        var recipient = createRecipientFromEmployee(mail, emp, circulationType);
        var saved = recipientRepository.save(recipient);
        rebuildToStr(mail);
        return recipientMapper.toResponse(saved);
    }

    /**
     * Batch add recipients with per-item success/fail reporting (fix B14: silent failure).
     */
    @Transactional
    public BatchRecipientResponse addBatch(Integer mailId, RecipientBatchRequest request, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        CirculationType circulationType = CirculationType.fromDbValue(request.circulation());
        Set<Integer> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        List<Long> empIdLongs = request.empIds().stream().map(Integer::longValue).toList();
        EmployeeResponse response = hrServiceClient.getBatchEmployees(
                new BatchIdsRequest(empIdLongs));
        List<EmployeeDto> employees = response.getData();

        Map<Long, EmployeeDto> empMap = employees.stream()
                .collect(Collectors.toMap(EmployeeDto::id, Function.identity()));

        List<MailRecipient> toSave = new ArrayList<>();
        List<BatchRecipientResponse.FailedRecipient> failed = new ArrayList<>();

        for (Integer empId : request.empIds()) {
            EmployeeDto emp = empMap.get(empId.longValue());
            if (emp == null) {
                failed.add(new BatchRecipientResponse.FailedRecipient(empId, "Employee not found in HR Service"));
                continue;
            }

            Integer userId = emp.id().intValue();
            if (existingUserIds.contains(userId)) {
                failed.add(new BatchRecipientResponse.FailedRecipient(empId, "Recipient already exists"));
                continue;
            }

            toSave.add(createRecipientFromEmployee(mail, emp, circulationType));
            existingUserIds.add(userId);
        }

        List<RecipientResponse> succeeded = recipientRepository.saveAll(toSave).stream()
                .map(recipientMapper::toResponse)
                .toList();

        rebuildToStr(mail);
        return BatchRecipientResponse.of(succeeded, failed, request.empIds().size());
    }

    @Transactional
    public void deleteRecipient(Integer mailId, Long recipientId, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        recipientRepository.deleteByMailIdAndId(mailId, recipientId);
        rebuildToStr(mail);
    }

    @Transactional
    public void deleteBatch(Integer mailId, List<Long> recipientIds, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        recipientRepository.deleteAllByMailIdAndIdIn(mailId, recipientIds);
        rebuildToStr(mail);
    }

    @Transactional
    public RecipientResponse updateNotifFlags(Integer mailId, Long recipientId,
                                               RecipientNotifPatchRequest request,
                                               Integer currentUserId) {
        assertCanManageRecipients(getMailOrThrow(mailId), currentUserId);
        var recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found: " + recipientId));

        if (request.emailNotif() != null) {
            recipient.setEmailNotif(request.emailNotif());
        }
        if (request.smsNotif() != null) {
            recipient.setSmsNotif(request.smsNotif());
        }

        return recipientMapper.toResponse(recipientRepository.save(recipient));
    }

    /**
     * Copy recipients from a reference mail for reply (excludes current user).
     */
    @Transactional
    public List<RecipientResponse> copyFrom(Integer mailId, Integer refMailId, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        Set<Integer> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        List<MailRecipient> source = recipientRepository.findByMailId(refMailId);
        List<MailRecipient> toSave = source.stream()
                .filter(r -> !r.getUserId().equals(currentUserId))
                .filter(r -> !existingUserIds.contains(r.getUserId()))
                .map(r -> {
                    var nr = new MailRecipient(mail, r.getUserId(), r.getEmpId(), CirculationType.REPLY);
                    nr.setEmpName(r.getEmpName());
                    nr.setPosName(r.getPosName());
                    nr.setPosId(r.getPosId());
                    return nr;
                })
                .toList();

        var saved = recipientRepository.saveAll(toSave).stream()
                .map(recipientMapper::toResponse)
                .toList();
        rebuildToStr(mail);
        return saved;
    }

    /**
     * Copy all distinct recipients from entire thread (reply-all).
     * Uses JOOQ to find distinct recipients across all mails in the thread.
     */
    @Transactional
    public List<RecipientResponse> copyThread(Integer mailId, Integer refMailId, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        Set<Integer> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        Mail refMail = getMailOrThrow(refMailId);
        Integer rootId = refMail.getRootMail() != null ? refMail.getRootMail().getId() : refMailId;

        var threadRecipients = recipientQueryRepository.findDistinctThreadRecipients(rootId);

        // Collect user IDs from thread recipients
        Set<Integer> threadUserIds = threadRecipients.stream()
                .map(RecipientQueryRepository.ThreadRecipientRow::userId)
                .collect(Collectors.toSet());

        List<MailRecipient> toSave = new ArrayList<>(threadRecipients.stream()
                .filter(r -> !r.userId().equals(currentUserId))
                .filter(r -> !existingUserIds.contains(r.userId()))
                .map(r -> {
                    var nr = new MailRecipient(mail, r.userId(), r.empId(), CirculationType.REPLY);
                    nr.setEmpName(r.empName());
                    nr.setPosName(r.posName());
                    nr.setPosId(r.posId());
                    return nr;
                })
                .toList());

        // Include root mail originator if not already in thread and not current user (analysis 1.6 step 4)
        Mail rootMail = getMailOrThrow(rootId);
        Integer originatorId = rootMail.getCreatedBy();
        if (originatorId != null
                && !originatorId.equals(currentUserId)
                && !threadUserIds.contains(originatorId)
                && !existingUserIds.contains(originatorId)) {
            var originator = new MailRecipient(mail, originatorId, originatorId, CirculationType.REPLY);
            originator.setEmpName(rootMail.getCreatedByName());
            toSave.add(originator);
        }

        var saved = recipientRepository.saveAll(toSave).stream()
                .map(recipientMapper::toResponse)
                .toList();
        rebuildToStr(mail);
        return saved;
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

    private void rebuildToStr(Mail mail) {
        List<MailRecipient> recipients = recipientRepository.findByMailId(mail.getId());
        mail.setToStr(Mail.buildToStr(recipients));
        mailRepository.save(mail);
    }

    private void assertCanManageRecipients(Mail mail, Integer currentUserId) {
        if (!mail.getCreatedBy().equals(currentUserId)) {
            throw new AccessDeniedException("Only the mail creator can manage recipients");
        }
    }
}
