package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailCommandService;
import id.perumdamts.mail.service.core.mail.MailQueryService;
import jakarta.validation.Valid;
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

    public MailController(MailCommandService commandService, MailQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
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
            @PathVariable Integer id,
            @Valid @RequestBody MailUpdateRequest request) {
        return commandService.updateDraft(id, request, principal);
    }

    @PostMapping("/{id}/send")
    public MailResponse send(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        return commandService.send(id, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        commandService.deleteMail(id, principal);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        commandService.restoreMail(id, principal);
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        commandService.markRead(id, principal);
    }

    @GetMapping("/{id}/thread")
    public List<MailSummaryResponse> getThread(@PathVariable Integer id) {
        return queryService.getThread(id);
    }

    @GetMapping("/search")
    public PagedResponse<MailSummaryResponse> search(MailSearchRequest request) {
        return queryService.search(request);
    }

    @GetMapping("/report")
    public PagedResponse<MailReportResponse> report(MailReportRequest request) {
        return queryService.getReport(request);
    }
}
