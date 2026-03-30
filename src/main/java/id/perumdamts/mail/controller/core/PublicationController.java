package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.publication.PublicationCommandService;
import id.perumdamts.mail.service.core.publication.PublicationQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publications")
public class PublicationController {

    private final PublicationCommandService commandService;
    private final PublicationQueryService queryService;

    public PublicationController(PublicationCommandService commandService,
                                  PublicationQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

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
            @PathVariable Integer id,
            @Valid @RequestPart("data") UpdatePublicationRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return commandService.update(id, request, file, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal MailPrincipal principal,
            @PathVariable Integer id) {
        commandService.delete(id, principal);
    }

    @GetMapping
    public List<PublicationDto> list(@ParameterObject PublicationParams params) {
        return queryService.list(params);
    }

    @GetMapping("/{id}")
    public PublicationDto findById(@PathVariable Integer id) {
        return queryService.findById(id);
    }
}
