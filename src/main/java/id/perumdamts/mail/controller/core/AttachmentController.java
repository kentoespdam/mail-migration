package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.dto.core.attachment.AttachmentMapper;
import id.perumdamts.mail.service.core.attachment.AttachmentCommandService;
import id.perumdamts.mail.service.core.attachment.AttachmentQueryService;
import id.perumdamts.mail.entity.core.Attachment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentQueryService queryService;
    private final AttachmentCommandService commandService;
    private final AttachmentMapper mapper;

    public AttachmentController(AttachmentQueryService queryService, AttachmentCommandService commandService, AttachmentMapper mapper) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AttachmentResponse> findByOwner(@RequestParam AttachmentRefType refType,
                                                 @RequestParam Long refId) {
        return queryService.findByOwner(refType, refId);
    }

    @GetMapping("/{id}")
    public AttachmentResponse findById(@PathVariable Integer id) {
        return queryService.findById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam AttachmentRefType refType,
            @RequestParam Long refId,
            @RequestParam(required = false) String docNotes,
            @AuthenticationPrincipal MailPrincipal principal) {
        Attachment attachment = commandService.upload(file, refType, refId, docNotes, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(attachment));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id,
                                             @AuthenticationPrincipal MailPrincipal principal) {
        Resource resource = queryService.download(id, principal);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        commandService.delete(id);
    }
}
