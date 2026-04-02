package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.documentType.*;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.service.master.DocumentTypeService;
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

    private final DocumentTypeService service;
    private final SqidsEncoder encoder;

    @GetMapping
    public PagedModel<DocumentTypeResponse> findAll(@ParameterObject DocumentTypeParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/lookup")
    public List<DocumentTypeLookup> lookup() {
        return service.lookup();
    }

    @GetMapping("/{id}")
    public DocumentTypeResponse findById(@PathVariable String id) {
        long rawId = encoder.decode(DocumentType.class, id);
        return service.findById(rawId);
    }

    @PostMapping
    public ResponseEntity<DocumentTypeResponse> create(@Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public DocumentTypeResponse update(@PathVariable String id, @Valid @RequestBody DocumentTypeRequest request) {
        long rawId = encoder.decode(DocumentType.class, id);
        return service.update(rawId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        long rawId = encoder.decode(DocumentType.class, id);
        service.delete(rawId);
    }
}
