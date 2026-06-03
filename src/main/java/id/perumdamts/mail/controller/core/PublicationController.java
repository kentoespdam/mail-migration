package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.publication.PublicationCommandHandler;
import id.perumdamts.mail.service.core.publication.PublicationQueryHandler;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationCommandHandler commandHandler;
    private final PublicationQueryHandler queryHandler;
    private final SqidsEncoder encoder;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicationResponse> create(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @ModelAttribute CreatePublicationRequest request) {
        var result = commandHandler.create(request, principal);
        long rawId = encoder.decode(Publication.class, result.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queryHandler.findById(rawId));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PublicationResponse update(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id,
            @Valid @ModelAttribute UpdatePublicationRequest request) {
        long rawId = encoder.decode(Publication.class, id);
        commandHandler.update(rawId, request, principal);
        return queryHandler.findById(rawId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long rawId = encoder.decode(Publication.class, id);
        commandHandler.delete(rawId, principal);
    }

    @PatchMapping("/{id}/publish")
    public PublicationResponse publish(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long rawId = encoder.decode(Publication.class, id);
        commandHandler.publish(rawId, principal);
        return queryHandler.findById(rawId);
    }

    @GetMapping
    public PagedModel<PublicationResponse> findAll(@Valid @ParameterObject PublicationParams params) {
        return new PagedModel<>(queryHandler.findAll(params));
    }


    @GetMapping("/{id}")
    public PublicationResponse findById(@PathVariable String id) {
        long rawId = encoder.decode(Publication.class, id);
        return queryHandler.findById(rawId);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        long rawId = encoder.decode(Publication.class, id);
        var result = queryHandler.download(rawId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .body(result.resource());
    }
}
