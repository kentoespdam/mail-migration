package id.perumdamts.mail.service.core.recipient;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.dto.id.UserId;
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
import id.perumdamts.mail.service.core.usertask.UserTaskCommandService;
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
public class MailRecipientCommandService {

    private final MailRecipientRepository recipientRepository;
    private final MailRepository mailRepository;
    private final HrServiceClient hrServiceClient;
    private final RecipientMapper recipientMapper;
    private final RecipientQueryRepository recipientQueryRepository;
    private final UserTaskCommandService userTaskCommandService;

    public MailRecipientCommandService(MailRecipientRepository recipientRepository,
                                       MailRepository mailRepository,
                                       HrServiceClient hrServiceClient,
                                       RecipientMapper recipientMapper,
                                       RecipientQueryRepository recipientQueryRepository,
                                       UserTaskCommandService userTaskCommandService) {
        this.recipientRepository = recipientRepository;
        this.mailRepository = mailRepository;
        this.hrServiceClient = hrServiceClient;
        this.recipientMapper = recipientMapper;
        this.recipientQueryRepository = recipientQueryRepository;
        this.userTaskCommandService = userTaskCommandService;
    }

    @Transactional
    public RecipientResponse addRecipient(Long mailId, RecipientRequest request, Long currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        CirculationType circulationType = CirculationType
                .fromDbValue((int) request.circulation().value());

        EmployeeDto emp = hrServiceClient.getEmployee(request.empId().value())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.empId()));

        Long userId = emp.id();
        if (recipientRepository.existsByMailIdAndUserId(mailId, userId)) {
            throw new IllegalArgumentException("Recipient already exists for this mail");
        }

        var recipient = createRecipientFromEmployee(mail, emp, circulationType);
        var saved = recipientRepository.save(recipient);

        if (mail.isSent()) {
            userTaskCommandService.createInboxes(mailId, List.of(userId));
        }

        rebuildToStr(mail);
        return recipientMapper.toResponse(saved);
    }

    /**
     * Batch add recipients with per-item success/fail reporting (fix B14: silent
     * failure).
     */
    @Transactional
    public BatchRecipientResponse addBatch(Long mailId, RecipientBatchRequest request, Long currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        CirculationType circulationType = CirculationType
                .fromDbValue((int) request.circulation().value());
        Set<Long> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        List<Long> empIdLongs = request.empIds().stream()
                .map(UserId::value)
                .toList();
        EmployeeResponse response = hrServiceClient.getBatchEmployees(
                new BatchIdsRequest(empIdLongs));
        List<EmployeeDto> employees = response.getData();

        Map<Long, EmployeeDto> empMap = employees.stream()
                .collect(Collectors.toMap(EmployeeDto::id, Function.identity()));

        List<MailRecipient> toSave = new ArrayList<>();
        List<BatchRecipientResponse.FailedRecipient> failed = new ArrayList<>();

        for (UserId empId : request.empIds()) {
            Long empLongId = empId.value();
            EmployeeDto emp = empMap.get(empLongId);
            if (emp == null) {
                failed.add(new BatchRecipientResponse.FailedRecipient(empId.toString(), "Employee not found in HR Service"));
                continue;
            }

            Long userId = emp.id();
            if (existingUserIds.contains(userId)) {
                failed.add(new BatchRecipientResponse.FailedRecipient(empId.toString(), "Recipient already exists"));
                continue;
            }

            toSave.add(createRecipientFromEmployee(mail, emp, circulationType));
            existingUserIds.add(userId);
        }

        List<RecipientResponse> succeeded = recipientRepository.saveAll(toSave).stream()
                .map(recipientMapper::toResponse)
                .toList();

        if (mail.isSent()) {
            List<Long> userIds = toSave.stream().map(MailRecipient::getUserId).toList();
            userTaskCommandService.createInboxes(mailId, userIds);
        }

        rebuildToStr(mail);
        return BatchRecipientResponse.of(succeeded, failed, request.empIds().size());
    }

    @Transactional
    public void deleteRecipient(Long mailId, Long recipientId, Long currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        recipientRepository.deleteByMailIdAndId(mailId, recipientId);
        rebuildToStr(mail);
    }

    @Transactional
    public void deleteBatch(Long mailId, List<Long> recipientIds, Long currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        assertCanManageRecipients(mail, currentUserId);
        recipientRepository.deleteAllByMailIdAndIdIn(mailId, recipientIds);
        rebuildToStr(mail);
    }

    @Transactional
    public RecipientResponse updateNotifFlags(Long mailId, Long recipientId,
                                              RecipientNotifPatchRequest request,
                                              Long currentUserId) {
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
    public List<RecipientResponse> copyFrom(Long mailId, Long refMailId, Long currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        Set<Long> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

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

        if (mail.isSent()) {
            List<Long> userIds = toSave.stream().map(MailRecipient::getUserId).toList();
            userTaskCommandService.createInboxes(mailId, userIds);
        }

        rebuildToStr(mail);
        return saved;
    }

    /**
     * Copy all distinct recipients from entire thread (reply-all).
     * Uses JOOQ to find distinct recipients across all mails in the thread.
     */
    @Transactional
    public List<RecipientResponse> copyThread(Long mailId, Long refMailId, Long currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        Set<Long> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        Mail refMail = getMailOrThrow(refMailId);
        Long rootId = refMail.getRootMail() != null ? refMail.getRootMail().getId() : refMailId;

        var threadRecipients = recipientQueryRepository.findDistinctThreadRecipients(rootId);

        Set<Long> threadUserIds = threadRecipients.stream()
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

        Mail rootMail = getMailOrThrow(rootId);
        Long originatorId = rootMail.getCreatedBy();
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

        if (mail.isSent()) {
            List<Long> userIds = toSave.stream().map(MailRecipient::getUserId).toList();
            userTaskCommandService.createInboxes(mailId, userIds);
        }

        rebuildToStr(mail);
        return saved;
    }

    private MailRecipient createRecipientFromEmployee(Mail mail, EmployeeDto emp, CirculationType circulationType) {
        var recipient = new MailRecipient(mail, emp.id(), emp.id(), circulationType);
        recipient.setEmpName(emp.nama());
        if (emp.jabatanId() != null) {
            recipient.setPosId(emp.jabatanId());
        }
        if (emp.jabatanNama() != null) {
            recipient.setPosName(emp.jabatanNama());
        }
        return recipient;
    }

    private Mail getMailOrThrow(Long mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
    }

    private void rebuildToStr(Mail mail) {
        List<MailRecipient> recipients = recipientRepository.findByMailId(mail.getId());
        mail.setToStr(Mail.buildToStr(recipients));
        mailRepository.save(mail);
    }

    private void assertCanManageRecipients(Mail mail, Long currentUserId) {
        if (!mail.getCreatedBy().equals(currentUserId)) {
            throw new AccessDeniedException("Only the mail creator can manage recipients");
        }
    }
}
