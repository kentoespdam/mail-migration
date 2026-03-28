package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.recipient.MailRecipientService;
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
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @Valid @RequestBody RecipientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.addRecipient(mailId, request, parseUserId(principal)));
    }

    @DeleteMapping("/{rid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipient(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @PathVariable Long rid) {
        recipientService.deleteRecipient(mailId, rid, parseUserId(principal));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @RequestBody RecipientDeleteBatchRequest request) {
        recipientService.deleteBatch(mailId, request.ids(), parseUserId(principal));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchRecipientResponse> addBatch(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @Valid @RequestBody RecipientBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.addBatch(mailId, request, parseUserId(principal)));
    }

    @PatchMapping("/{rid}")
    public RecipientResponse updateNotifFlags(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @PathVariable Long rid,
            @Valid @RequestBody RecipientNotifPatchRequest request) {
        return recipientService.updateNotifFlags(mailId, rid, request, parseUserId(principal));
    }

    @PostMapping("/copy-from/{refId}")
    public List<RecipientResponse> copyFrom(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @PathVariable Integer refId) {
        return recipientService.copyFrom(mailId, refId, parseUserId(principal));
    }

    @PostMapping("/copy-thread/{refId}")
    public List<RecipientResponse> copyThread(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer mailId,
            @PathVariable Integer refId) {
        return recipientService.copyThread(mailId, refId, parseUserId(principal));
    }

    private Integer parseUserId(MailPrincipal principal) {
        return Integer.parseInt(principal.userId());
    }
}
