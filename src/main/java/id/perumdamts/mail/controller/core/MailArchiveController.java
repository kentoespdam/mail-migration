package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.archive.*;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.archive.MailArchiveCommandService;
import id.perumdamts.mail.service.core.archive.MailArchiveQueryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/archives")
public class MailArchiveController {

    private final MailArchiveCommandService commandService;
    private final MailArchiveQueryService queryService;

    public MailArchiveController(MailArchiveCommandService commandService,
            MailArchiveQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ArchiveResponse> createDraft(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody ArchiveCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createDraft(request, principal));
    }

    @PutMapping("/{id}")
    public ArchiveResponse updateDraft(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ArchiveUpdateRequest request) {
        return commandService.updateDraft(id, request, principal);
    }

    @PostMapping("/{id}/publish")
    public ArchiveResponse publish(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Long id) {
        return commandService.publishArchive(id, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArchive(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Long id) {
        commandService.deleteArchive(id, principal);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<ArchiveSummaryResponse> findForAdmin(@ParameterObject ArchiveSearchRequest request) {
        return queryService.findForAdmin(request);
    }

    @GetMapping
    public PagedResponse<ArchiveSummaryResponse> searchWithAcl(
            @AuthenticationPrincipal MailPrincipal principal,
            @ParameterObject ArchiveSearchRequest request,
            @RequestParam List<Long> positionIds) {
        return queryService.searchWithAcl(request, positionIds);
    }

    @GetMapping("/{id}/access")
    public List<ArchiveAccessResponse> getAccess(@PathVariable Long id) {
        return commandService.getAccess(id);
    }

    @PutMapping("/{id}/access")
    public List<ArchiveAccessResponse> setAccess(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ArchiveAccessRequest request) {
        return commandService.setAccess(id, request, principal);
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<ArchiveReportResponse> report(@ParameterObject ArchiveReportRequest request) {
        return queryService.getReport(request);
    }
}
