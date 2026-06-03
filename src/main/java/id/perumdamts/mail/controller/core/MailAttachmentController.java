package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.attachment.AttachmentDetailResponse;
import id.perumdamts.mail.dto.core.attachment.AttachmentMapper;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.attachment.AttachmentCommandService;
import id.perumdamts.mail.service.core.attachment.AttachmentQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
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
    private final SqidsEncoder encoder;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDetailResponse> upload(
            @PathVariable String mailId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String docNotes,
            @AuthenticationPrincipal MailPrincipal principal) {
        long id = encoder.decode(Mail.class, mailId);
        Attachment attachment = commandService.uploadAttachment(file, id, docNotes, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDetailResponse(attachment));
    }

    @GetMapping
    public List<AttachmentResponse> getAttachments(
            @PathVariable String mailId,
            @AuthenticationPrincipal MailPrincipal principal) {
        long id = encoder.decode(Mail.class, mailId);
        return queryService.getAttachmentsByMailId(id, principal);
    }

    @GetMapping("/{attachmentId}")
    public AttachmentDetailResponse getAttachmentDetail(
            @PathVariable String mailId,
            @PathVariable String attachmentId,
            @AuthenticationPrincipal MailPrincipal principal) {
        long mId = encoder.decode(Mail.class, mailId);
        int aId = (int) encoder.decode(Attachment.class, attachmentId);
        return queryService.getAttachmentDetail(aId, mId, principal);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String mailId,
            @PathVariable String attachmentId,
            @AuthenticationPrincipal MailPrincipal principal) {
        long mId = encoder.decode(Mail.class, mailId);
        int aId = (int) encoder.decode(Attachment.class, attachmentId);
        Resource resource = queryService.downloadAttachment(aId, mId, principal);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(
            @PathVariable String mailId,
            @PathVariable String attachmentId,
            @AuthenticationPrincipal MailPrincipal principal) {
        long mId = encoder.decode(Mail.class, mailId);
        int aId = (int) encoder.decode(Attachment.class, attachmentId);
        commandService.deleteAttachment(aId, mId, principal);
    }
}
