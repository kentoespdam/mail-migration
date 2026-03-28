package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.attachment.AttachmentService;
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

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AttachmentResponse> findByOwner(@RequestParam AttachmentRefType refType,
                                                 @RequestParam Long refId) {
        return service.findByOwner(refType, refId);
    }

    @GetMapping("/{id}")
    public AttachmentResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam AttachmentRefType refType,
            @RequestParam Long refId,
            @RequestParam(required = false) String docNotes,
            @AuthenticationPrincipal MailPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.upload(file, refType, refId, docNotes, principal));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id,
                                             @AuthenticationPrincipal MailPrincipal principal) {
        var result = service.download(id, principal);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .body(result.resource());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
