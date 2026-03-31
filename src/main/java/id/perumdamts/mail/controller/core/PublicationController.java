package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.publication.PublicationCommandService;
import id.perumdamts.mail.service.core.publication.PublicationQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationCommandService commandService;
    private final PublicationQueryService queryService;
    private final SqidsEncoder encoder;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicationDto> create(
            @AuthenticationPrincipal MailPrincipal principal,
            @Valid @RequestPart("data") CreatePublicationRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.create(request, file, principal));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PublicationDto update(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id,
            @Valid @RequestPart("data") UpdatePublicationRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        long rawId = encoder.decode(Publication.class, id);
        return commandService.update(rawId, request, file, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable String id) {
        long rawId = encoder.decode(Publication.class, id);
        commandService.delete(rawId, principal);
    }

    @GetMapping
    public PagedResponse<PublicationDto> list(@ParameterObject PublicationParams params) {
        log.debug("kentoes sort {}", params.toSortField());
        return queryService.list(params);
    }

    @GetMapping("/{id}")
    public PublicationDto findById(@PathVariable String id) {
        long rawId = encoder.decode(Publication.class, id);
        return queryService.findById(rawId);
    }
}
