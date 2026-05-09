package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.DispositionStatusDeriver;
import id.perumdamts.mail.service.core.mail.MailCommandService;
import id.perumdamts.mail.service.core.mail.MailQueryService;
import id.perumdamts.mail.service.core.recipient.MailRecipientQueryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mails")
public class MailController {

    private final MailCommandService commandService;
    private final MailQueryService queryService;
    private final MailRecipientQueryService recipientQueryService;
    private final DispositionStatusDeriver dispositionStatusDeriver;

    public MailController(MailCommandService commandService,
            MailQueryService queryService,
            MailRecipientQueryService recipientQueryService,
            DispositionStatusDeriver dispositionStatusDeriver) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.recipientQueryService = recipientQueryService;
        this.dispositionStatusDeriver = dispositionStatusDeriver;
    }

    @PostMapping
    public ResponseEntity<MailResponse> createDraft(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody MailCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createDraft(request, principal));
    }

    @PostMapping("/send")
    public ResponseEntity<MailResponse> sendMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody MailSendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.sendMail(request, principal));
    }

    @PutMapping("/{id}")
    public MailResponse updateDraft(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId id,
            @Valid @RequestBody MailUpdateRequest request) {
        return commandService.updateDraft(id.value(), request, principal);
    }

    @PostMapping("/{id}/send")
    public MailResponse send(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId id) {
        return commandService.send(id.value(), principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId id) {
        commandService.deleteMail(id.value(), principal);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId id) {
        commandService.restoreMail(id.value(), principal);
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId id) {
        commandService.markRead(id.value(), principal);
    }

    @GetMapping("/lookup")
    public PagedModel<MailLookupResponse> lookup(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @ParameterObject MailLookupParams params) {
        return new PagedModel<>(queryService.findLookup(principal.userIdLong(), params));
    }

    @GetMapping("/{id}")
    public MailResponse getById(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable MailId id) {
        // Auto mark-read
        try {
            commandService.markRead(id.value(), principal);
        } catch (Exception e) {
            // Ignore if UserTask not found (e.g. creator accessing detail)
        }
        return queryService.getDetail(id.value(), principal.userIdLong());
    }

    @GetMapping("/{id}/tracking")
    public List<MailTrackingResponse> getTracking(@PathVariable MailId id) {
        return recipientQueryService.findTracking(id.value());
    }

    @GetMapping("/{id}/read-status")
    public List<RecipientReadStatusResponse> getReadStatus(@PathVariable MailId id) {
        return recipientQueryService.findReadStatus(id.value());
    }

    @GetMapping("/{id}/thread")
    public List<MailSummaryResponse> getThread(@PathVariable MailId id) {
        return queryService.findThread(id.value());
    }

    @GetMapping("/search")
    public PagedModel<MailSummaryResponse> search(@Valid @ParameterObject MailSearchRequest request) {
        return new PagedModel<>(queryService.search(request));
    }

    @GetMapping("/report")
    public PagedModel<MailReportResponse> report(@Valid @ParameterObject  MailReportRequest request) {
        return new PagedModel<>(queryService.findReport(request));
    }

    @GetMapping("/{id}/disposition-status")
    @PreAuthorize("isAuthenticated()")
    public DispositionStatusResponse getDispositionStatus(@PathVariable MailId id) {
        var result = dispositionStatusDeriver.deriveStatus(id.value());
        if (result == null) {
            return null;
        }
        return new DispositionStatusResponse(result.status(), result.deadline(), result.depth());
    }
}
