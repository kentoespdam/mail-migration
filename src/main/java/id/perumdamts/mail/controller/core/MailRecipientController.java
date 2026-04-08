package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.recipient.MailRecipientCommandService;
import id.perumdamts.mail.service.core.recipient.MailRecipientQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mails/{mailId}/recipients")
public class MailRecipientController {

    private final MailRecipientQueryService queryService;
    private final MailRecipientCommandService commandService;
    private final SqidsEncoder encoder;

    public MailRecipientController(MailRecipientQueryService queryService,
            MailRecipientCommandService commandService,
            SqidsEncoder encoder) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.encoder = encoder;
    }

    @GetMapping
    public List<RecipientResponse> getRecipients(@PathVariable String mailId) {
        long id = encoder.decode(Mail.class, mailId);
        return queryService.getRecipients(id);
    }

    @PostMapping
    public ResponseEntity<RecipientResponse> addRecipient(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @Valid @RequestBody RecipientRequest request) {
        long id = encoder.decode(Mail.class, mailId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addRecipient(id, request, principal.userIdLong()));
    }

    @DeleteMapping("/{rid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipient(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @PathVariable String rid) {
        long id = encoder.decode(Mail.class, mailId);
        long recipientId = encoder.decode(MailRecipient.class, rid);
        commandService.deleteRecipient(id, recipientId, principal.userIdLong());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @RequestBody RecipientDeleteBatchRequest request) {
        long id = encoder.decode(Mail.class, mailId);
        List<Long> recipientIds = request.ids().stream()
                .map(sqid -> encoder.decode(MailRecipient.class, sqid))
                .toList();
        commandService.deleteBatch(id, recipientIds, principal.userIdLong());
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchRecipientResponse> addBatch(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @Valid @RequestBody RecipientBatchRequest request) {
        long id = encoder.decode(Mail.class, mailId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addBatch(id, request, principal.userIdLong()));
    }

    @PatchMapping("/{rid}")
    public RecipientResponse updateNotifFlags(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @PathVariable String rid,
            @Valid @RequestBody RecipientNotifPatchRequest request) {
        long id = encoder.decode(Mail.class, mailId);
        long recipientId = encoder.decode(MailRecipient.class, rid);
        return commandService.updateNotifFlags(id, recipientId, request, principal.userIdLong());
    }

    @PostMapping("/copy-from/{refId}")
    public List<RecipientResponse> copyFrom(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @PathVariable String refId) {
        long id = encoder.decode(Mail.class, mailId);
        long refMailId = encoder.decode(Mail.class, refId);
        return commandService.copyFrom(id, refMailId, principal.userIdLong());
    }

    @PostMapping("/copy-thread/{refId}")
    public List<RecipientResponse> copyThread(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String mailId,
            @PathVariable String refId) {
        long id = encoder.decode(Mail.class, mailId);
        long refMailId = encoder.decode(Mail.class, refId);
        return commandService.copyThread(id, refMailId, principal.userIdLong());
    }

}
