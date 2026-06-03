package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.attachment.AttachmentDetailResponse;
import id.perumdamts.mail.dto.core.attachment.AttachmentMapper;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.dto.id.AttachmentId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.attachment.AttachmentCommandService;
import id.perumdamts.mail.service.core.attachment.AttachmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mails/{mailId}/attachments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MailAttachmentController {

    private final AttachmentCommandService commandService;
    private final AttachmentQueryService queryService;
    private final AttachmentMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDetailResponse> upload(
            @PathVariable MailId mailId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String docNotes,
            @AuthenticationPrincipal MailPrincipal principal) {
        Attachment attachment = commandService.uploadAttachment(file, mailId.value(), docNotes, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDetailResponse(attachment));
    }

    @GetMapping
    public List<AttachmentResponse> getAttachments(
            @PathVariable MailId mailId,
            @AuthenticationPrincipal MailPrincipal principal) {
        return queryService.getAttachmentsByMailId(mailId.value(), principal);
    }

    @GetMapping("/{attachmentId}")
    public AttachmentDetailResponse getAttachmentDetail(
            @PathVariable MailId mailId,
            @PathVariable AttachmentId attachmentId,
            @AuthenticationPrincipal MailPrincipal principal) {
        return queryService.getAttachmentDetail(attachmentId.value(), mailId.value(), principal);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable MailId mailId,
            @PathVariable AttachmentId attachmentId,
            @AuthenticationPrincipal MailPrincipal principal) {
        Resource resource = queryService.downloadAttachment(attachmentId.value(), mailId.value(), principal);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(
            @PathVariable MailId mailId,
            @PathVariable AttachmentId attachmentId,
            @AuthenticationPrincipal MailPrincipal principal) {
        commandService.deleteAttachment(attachmentId.value(), mailId.value(), principal);
    }
}
