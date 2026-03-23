package id.perumdamts.mail.api.rest;

import id.perumdamts.mail.api.dto.folder.FolderCounterResponse;
import id.perumdamts.mail.api.dto.folder.MailFolderRequest;
import id.perumdamts.mail.api.dto.folder.MailFolderResponse;
import id.perumdamts.mail.api.dto.folder.MoveMailRequest;
import id.perumdamts.mail.api.dto.mail.MailSummaryResponse;
import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.infrastructure.security.MailPrincipal;
import id.perumdamts.mail.service.folder.MailFolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class MailFolderController {

    private final MailFolderService folderService;
    private final TenantConfig tenantConfig;

    public MailFolderController(MailFolderService folderService, TenantConfig tenantConfig) {
        this.folderService = folderService;
        this.tenantConfig = tenantConfig;
    }

    /**
     * Get folder tree untuk user (system + personal folders) dengan counter badge.
     */
    @GetMapping("/folders")
    public List<MailFolderResponse> getFolderTree(@AuthenticationPrincipal MailPrincipal principal) {
        return folderService.getFolderTree(Integer.parseInt(principal.userId()));
    }

    /**
     * Get counter badge per folder (unread/total).
     */
    @GetMapping("/folders/counters")
    public List<FolderCounterResponse> getCounters(@AuthenticationPrincipal MailPrincipal principal) {
        return folderService.getCounters(Integer.parseInt(principal.userId()));
    }

    /**
     * Create personal folder baru.
     */
    @PostMapping("/folders")
    public ResponseEntity<MailFolderResponse> createFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody MailFolderRequest request) {
        var response = folderService.createFolder(Integer.parseInt(principal.userId()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Rename personal folder.
     */
    @PutMapping("/folders/{id}")
    public MailFolderResponse renameFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id,
            @Valid @RequestBody MailFolderRequest request) {
        return folderService.renameFolder(Integer.parseInt(principal.userId()), id, request);
    }

    /**
     * Delete personal folder (soft delete).
     */
    @DeleteMapping("/folders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        folderService.deleteFolder(Integer.parseInt(principal.userId()), id);
    }

    /**
     * List mails dalam folder (paginated).
     */
    @GetMapping("/folders/{id}/mails")
    public List<MailSummaryResponse> getMailsInFolder(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return folderService.getMailsInFolder(
                Integer.parseInt(principal.userId()), id, page, size,
                tenantConfig.inboxSortAscending());
    }

    /**
     * Move mails dari satu folder ke folder lain.
     */
    @PutMapping("/folders/move")
    public void moveMails(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestBody MoveMailRequest request) {
        folderService.moveMails(Integer.parseInt(principal.userId()), request);
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
            @PathVariable Integer id) {
        folderService.deleteMail(Integer.parseInt(principal.userId()), id);
    }

    /**
     * Restore mail dari trash ke folder asal.
     * restore_folder_id diambil dari DB (bukan dari client) — fix issue B4.
     */
    @PostMapping("/mails/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreMail(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        folderService.restoreMail(Integer.parseInt(principal.userId()), id);
    }

    /**
     * Empty trash — purge semua mail di folder DELETED.
     */
    @DeleteMapping("/trash")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyTrash(@AuthenticationPrincipal MailPrincipal principal) {
        folderService.emptyTrash(Integer.parseInt(principal.userId()));
    }
}
