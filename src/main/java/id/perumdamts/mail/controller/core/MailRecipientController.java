package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.id.MailRecipientId;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.recipient.MailRecipientCommandService;
import id.perumdamts.mail.service.core.recipient.MailRecipientQueryService;
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

    public MailRecipientController(MailRecipientQueryService queryService,
            MailRecipientCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping
    public List<RecipientResponse> getRecipients(@PathVariable MailId mailId) {
        return queryService.findRecipients(mailId.value());
    }

    @PostMapping
    public ResponseEntity<RecipientResponse> addRecipient(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @Valid @RequestBody RecipientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addRecipient(mailId.value(), request, principal.userIdLong()));
    }

    @DeleteMapping("/{rid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipient(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @PathVariable MailRecipientId rid) {
        commandService.deleteRecipient(mailId.value(), rid.value(), principal.userIdLong());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @RequestBody RecipientDeleteBatchRequest request) {
        List<Long> recipientIds = request.ids().stream()
                .map(MailRecipientId::value)
                .toList();
        commandService.deleteBatch(mailId.value(), recipientIds, principal.userIdLong());
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchRecipientResponse> addBatch(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @Valid @RequestBody RecipientBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addBatch(mailId.value(), request, principal.userIdLong()));
    }

    @PatchMapping("/{rid}")
    public RecipientResponse updateNotifFlags(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @PathVariable MailRecipientId rid,
            @Valid @RequestBody RecipientNotifPatchRequest request) {
        return commandService.updateNotifFlags(mailId.value(), rid.value(), request, principal.userIdLong());
    }

    @PostMapping("/copy-from/{refId}")
    public List<RecipientResponse> copyFrom(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @PathVariable MailId refId) {
        return commandService.copyFrom(mailId.value(), refId.value(), principal.userIdLong());
    }

    @PostMapping("/copy-thread/{refId}")
    public List<RecipientResponse> copyThread(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId mailId,
            @PathVariable MailId refId) {
        return commandService.copyThread(mailId.value(), refId.value(), principal.userIdLong());
    }

}
