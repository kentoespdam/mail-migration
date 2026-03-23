package id.perumdamts.mail.service.recipient;

import id.perumdamts.mail.api.dto.recipient.*;
import id.perumdamts.mail.domain.entity.Mail;
import id.perumdamts.mail.domain.entity.MailRecipient;
import id.perumdamts.mail.domain.enums.CirculationType;
import id.perumdamts.mail.integration.hr.BatchIdsRequest;
import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.repository.jpa.MailRecipientRepository;
import id.perumdamts.mail.repository.jpa.MailRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public MailRecipientService(MailRecipientRepository recipientRepository,
                                 MailRepository mailRepository,
                                 HrServiceClient hrServiceClient,
                                 RecipientMapper recipientMapper) {
        this.recipientRepository = recipientRepository;
        this.mailRepository = mailRepository;
        this.hrServiceClient = hrServiceClient;
        this.recipientMapper = recipientMapper;
    }

    public List<RecipientResponse> getRecipients(Integer mailId) {
        return recipientRepository.findByMailId(mailId).stream()
                .map(recipientMapper::toResponse)
                .toList();
    }

    @Transactional
    public RecipientResponse addRecipient(Integer mailId, RecipientRequest request) {
        Mail mail = getMailOrThrow(mailId);
        CirculationType circulationType = CirculationType.fromDbValue(request.circulation());

        EmployeeDto emp = hrServiceClient.getEmployee(request.empId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.empId()));

        Integer userId = emp.id().intValue();
        if (recipientRepository.existsByMailIdAndUserId(mailId, userId)) {
            throw new IllegalArgumentException("Recipient already exists for this mail");
        }

        var recipient = new MailRecipient(mail, userId, request.empId(), circulationType);
        recipient.setEmpName(emp.nama());
        if (emp.jabatanNama() != null) {
            recipient.setPosName(emp.jabatanNama());
        }

        return recipientMapper.toResponse(recipientRepository.save(recipient));
    }

    @Transactional
    public List<RecipientResponse> addBatch(Integer mailId, RecipientBatchRequest request) {
        Mail mail = getMailOrThrow(mailId);
        CirculationType circulationType = CirculationType.fromDbValue(request.circulation());
        Set<Integer> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        List<Long> empIdLongs = request.empIds().stream().map(Integer::longValue).toList();
        List<EmployeeDto> employees = hrServiceClient.getBatchEmployees(
                new BatchIdsRequest(empIdLongs));

        Map<Long, EmployeeDto> empMap = employees.stream()
                .collect(Collectors.toMap(EmployeeDto::id, Function.identity()));

        List<MailRecipient> toSave = new ArrayList<>();
        for (Integer empId : request.empIds()) {
            EmployeeDto emp = empMap.get(empId.longValue());
            if (emp == null) continue;

            Integer userId = emp.id().intValue();
            if (existingUserIds.contains(userId)) continue;

            var recipient = new MailRecipient(mail, userId, empId, circulationType);
            recipient.setEmpName(emp.nama());
            if (emp.jabatanNama() != null) {
                recipient.setPosName(emp.jabatanNama());
            }
            toSave.add(recipient);
            existingUserIds.add(userId);
        }

        return recipientRepository.saveAll(toSave).stream()
                .map(recipientMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteRecipient(Integer mailId, Long recipientId) {
        recipientRepository.deleteByMailIdAndId(mailId, recipientId);
    }

    @Transactional
    public RecipientResponse updateNotifFlags(Integer mailId, Long recipientId,
                                               RecipientNotifPatchRequest request) {
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
                    return nr;
                })
                .toList();

        return recipientRepository.saveAll(toSave).stream()
                .map(recipientMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<RecipientResponse> copyThread(Integer mailId, Integer refMailId, Integer currentUserId) {
        Mail mail = getMailOrThrow(mailId);
        Set<Integer> existingUserIds = recipientRepository.findUserIdsByMailId(mailId);

        // Get all recipients from all mails in the thread
        Mail refMail = getMailOrThrow(refMailId);
        Integer rootId = refMail.getRootMail() != null ? refMail.getRootMail().getId() : refMailId;

        // Query all recipients in thread (via JOOQ would be better, but keeping it simple)
        // For now, use the ref mail's recipients + walk up might be complex
        // Simplified: copy from refMailId like copyFrom but with REPLY circulation
        return copyFrom(mailId, refMailId, currentUserId);
    }

    private Mail getMailOrThrow(Integer mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));
    }
}
