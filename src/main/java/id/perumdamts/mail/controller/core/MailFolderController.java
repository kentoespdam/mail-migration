package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.folder.*;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.folder.MailFolderCommandService;
import id.perumdamts.mail.service.core.folder.MailFolderQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk MailFolder operations.
 * - GET /api/v1/mail/folders — folder tree dengan counter
 * - GET /api/v1/mail/folders/counters — badge counters
 * - POST/PUT/DELETE /api/v1/mail/folders — CRUD personal folder
 * - GET /api/v1/mail/folders/{id}/mails — list mail dalam folder
 * - PUT /api/v1/mail/folders/move — move mails
 * - DELETE /api/v1/mail/trash — empty trash
 * - POST /api/v1/mail/mails/{id}/delete — delete mail (2-level soft delete)
 * - POST /api/v1/mail/mails/{id}/restore — restore mail dari trash
 */
@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MailFolderController {

    private final MailFolderQueryService queryService;
    private final MailFolderCommandService commandService;
    private final SqidsEncoder encoder;

    /**
     * Get folder tree untuk user (system + personal folders) dengan counter badge.
     * Lazy create personal root folder on first access.
     */
    @GetMapping("/folders")
    public List<MailFolderResponse> getFolderTree(@AuthenticationPrincipal MailPrincipal principal) {
        commandService.ensureSystemFolders(principal.userIdLong());
        return queryService.getFolderTree(principal.userIdLong());
    }

    /**
     * Get counter badge per folder (unread/total).
     * Lazy create personal root folder on first access.
     */
    @GetMapping("/folders/counters")
    public List<FolderCounterResponse> getCounters(@AuthenticationPrincipal MailPrincipal principal) {
        commandService.ensureSystemFolders(principal.userIdLong());
        return queryService.getCounters(principal.userIdLong());
    }

    /**
     * Create personal folder baru.
     */
    @PostMapping("/folders")
    public ResponseEntity<MailFolderResponse> createFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody MailFolderRequest request) {
        var response = commandService.createFolder(principal.userIdLong(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Rename personal folder.
     */
    @PutMapping("/folders/{id}")
    public MailFolderResponse renameFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody MailFolderRequest request) {
        long rawId = encoder.decode(MailFolder.class, id);
        return commandService.renameFolder(principal.userIdLong(), rawId, request);
    }

    /**
     * Delete personal folder (soft delete).
     */
    @DeleteMapping("/folders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long rawId = encoder.decode(MailFolder.class, id);
        commandService.deleteFolder(principal.userIdLong(), rawId);
    }

    /**
     * List mails dalam folder (paginated).
     */
    @GetMapping("/folders/{id}/mails")
    public List<MailSummaryResponse> getMailsInFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id,
            @ParameterObject MailFolderMailsParams params) {
        long rawId = encoder.decode(MailFolder.class, id);
        return queryService.getMailsInFolder(
                principal.userIdLong(), rawId, params);
    }

    /**
     * Move mails dari satu folder ke folder lain.
     */
    @PutMapping("/folders/move")
    public void moveMails(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody MoveMailRequest request) {
        commandService.moveMails(principal, request);
    }

    /**
     * Delete mail — soft delete 2-level:
     * - Jika belum di trash → pindah ke DELETED(6)
     * - Jika sudah di trash → purge (hapus permanent)
     */
    @PostMapping("/mails/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        commandService.deleteMail(principal, mailId);
    }

    /**
     * Restore mail dari trash ke folder asal.
     * restore_folder_id diambil dari DB (bukan dari client) — fix issue B4.
     */
    @PostMapping("/mails/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long mailId = encoder.decode(Mail.class, id);
        commandService.restoreMail(principal, mailId);
    }

    /**
     * Empty trash — purge semua mail di folder DELETED.
     */
    @DeleteMapping("/trash")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyTrash(@AuthenticationPrincipal MailPrincipal principal) {
        commandService.emptyTrash(principal.userIdLong());
    }

}
