package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeParams;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeRequest;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeResponse;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.service.master.DocumentTypeCommandService;
import id.perumdamts.mail.service.master.DocumentTypeQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeCommandService commandService;
    private final DocumentTypeQueryService queryService;
    private final SqidsEncoder encoder;

    @GetMapping
    public PagedModel<DocumentTypeResponse> findAll(@ParameterObject DocumentTypeParams params) {
        return new PagedModel<>(queryService.findAll(params));
    }

    @GetMapping("/lookup")
    public List<DocumentTypeLookup> lookup() {
        return queryService.lookup();
    }

    @GetMapping("/{id}")
    public DocumentTypeResponse findById(@PathVariable String id) {
        long rawId = encoder.decode(DocumentType.class, id);
        return queryService.findById(rawId);
    }

    @PostMapping
    public ResponseEntity<DocumentTypeResponse> create(@Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(request));
    }

    @PutMapping("/{id}")
    public DocumentTypeResponse update(@PathVariable String id, @Valid @RequestBody DocumentTypeRequest request) {
        long rawId = encoder.decode(DocumentType.class, id);
        return commandService.update(rawId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        long rawId = encoder.decode(DocumentType.class, id);
        commandService.delete(rawId);
    }
}
