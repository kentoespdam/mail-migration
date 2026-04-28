package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailCommandService;
import id.perumdamts.mail.service.core.mail.MailQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mails")
public class MailController {

    private final MailCommandService commandService;
    private final MailQueryService queryService;
    private final SqidsEncoder encoder;

    public MailController(MailCommandService commandService, MailQueryService queryService, SqidsEncoder encoder) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.encoder = encoder;
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
            @PathVariable String id,
            @Valid @RequestBody MailUpdateRequest request) {
        long mailId = encoder.decode(Mail.class, id);
        return commandService.updateDraft(mailId, request, principal);
    }

    @PostMapping("/{id}/send")
    public MailResponse send(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        return commandService.send(mailId, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        commandService.deleteMail(mailId, principal);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        commandService.restoreMail(mailId, principal);
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        commandService.markRead(mailId, principal);
    }

    @GetMapping("/lookup")
    public PagedModel<MailLookupResponse> lookup(
            @AuthenticationPrincipal MailPrincipal principal,
            @ParameterObject MailLookupParams params) {
        return new PagedModel<>(queryService.lookup(principal.userIdLong(), params));
    }

    @GetMapping("/{id}")
    public MailResponse getById(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        // Auto mark-read
        try {
            commandService.markRead(mailId, principal);
        } catch (Exception e) {
            // Ignore if UserTask not found (e.g. creator accessing detail)
        }
        return queryService.getDetail(mailId);
    }

    @GetMapping("/{id}/tracking")
    public List<MailTrackingItemResponse> getTracking(@PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        return queryService.findThreadTracking(mailId);
    }

    @GetMapping("/{id}/read-status")
    public List<RecipientReadStatusResponse> getReadStatus(@PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        return queryService.getReadStatus(mailId);
    }

    @GetMapping("/{id}/thread")
    public List<MailSummaryResponse> getThread(@PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        return queryService.getThread(mailId);
    }

    @GetMapping("/search")
    public PagedResponse<MailSummaryResponse> search(@ParameterObject MailSearchRequest request) {
        return queryService.search(request);
    }

    @GetMapping("/report")
    public PagedResponse<MailReportResponse> report(@ParameterObject  MailReportRequest request) {
        return queryService.getReport(request);
    }

}
