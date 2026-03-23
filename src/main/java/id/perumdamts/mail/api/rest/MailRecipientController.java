package id.perumdamts.mail.api.rest;

import id.perumdamts.mail.api.dto.recipient.RecipientBatchRequest;
import id.perumdamts.mail.api.dto.recipient.RecipientNotifPatchRequest;
import id.perumdamts.mail.api.dto.recipient.RecipientRequest;
import id.perumdamts.mail.api.dto.recipient.RecipientResponse;
import id.perumdamts.mail.infrastructure.security.MailPrincipal;
import id.perumdamts.mail.service.recipient.MailRecipientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mails/{mailId}/recipients")
public class MailRecipientController {

    private final MailRecipientService recipientService;

    public MailRecipientController(MailRecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @GetMapping
    public List<RecipientResponse> getRecipients(@PathVariable Integer mailId) {
        return recipientService.getRecipients(mailId);
    }

    @PostMapping
    public ResponseEntity<RecipientResponse> addRecipient(
            @PathVariable Integer mailId,
            @Valid @RequestBody RecipientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.addRecipient(mailId, request));
    }

    @DeleteMapping("/{rid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipient(@PathVariable Integer mailId, @PathVariable Long rid) {
        recipientService.deleteRecipient(mailId, rid);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<RecipientResponse>> addBatch(
            @PathVariable Integer mailId,
            @Valid @RequestBody RecipientBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.addBatch(mailId, request));
    }

    @PatchMapping("/{rid}")
    public RecipientResponse updateNotifFlags(
            @PathVariable Integer mailId,
            @PathVariable Long rid,
            @Valid @RequestBody RecipientNotifPatchRequest request) {
        return recipientService.updateNotifFlags(mailId, rid, request);
    }

    @PostMapping("/copy-from/{refId}")
    public List<RecipientResponse> copyFrom(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @PathVariable Integer refId) {
        return recipientService.copyFrom(mailId, refId, Integer.parseInt(principal.userId()));
    }

    @PostMapping("/copy-thread/{refId}")
    public List<RecipientResponse> copyThread(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @PathVariable Integer refId) {
        return recipientService.copyThread(mailId, refId, Integer.parseInt(principal.userId()));
    }
}
