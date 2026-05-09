package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.dto.id.PublicationId;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.publication.PublicationCommandHandler;
import id.perumdamts.mail.service.core.publication.PublicationQueryHandler;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicationResponse> create(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @ModelAttribute CreatePublicationRequest request) {
        var result = commandHandler.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(queryHandler.findById(result.id().value()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PublicationResponse update(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable PublicationId id,
            @Valid @ModelAttribute UpdatePublicationRequest request) {
        commandHandler.update(id.value(), request, principal);
        return queryHandler.findById(id.value());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable PublicationId id) {
        commandHandler.delete(id.value(), principal);
    }

    @PatchMapping("/{id}/publish")
    public PublicationResponse publish(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable PublicationId id) {
        commandHandler.publish(id.value(), principal);
        return queryHandler.findById(id.value());
    }

    @GetMapping
    public PagedModel<PublicationResponse> findAll(@Valid @ParameterObject PublicationParams params) {
        return new PagedModel<>(queryHandler.findAll(params));
    }


    @GetMapping("/{id}")
    public PublicationResponse findById(@PathVariable PublicationId id) {
        return queryHandler.findById(id.value());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable PublicationId id) {
        var result = queryHandler.download(id.value());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .body(result.resource());
    }
}
