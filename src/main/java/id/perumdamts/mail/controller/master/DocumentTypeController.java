package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.id.DocumentTypeId;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeParams;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeRequest;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeResponse;
import id.perumdamts.mail.service.master.documentType.DocumentTypeCommandService;
import id.perumdamts.mail.service.master.documentType.DocumentTypeQueryService;
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

    @GetMapping
    public PagedModel<DocumentTypeResponse> findAll(@ParameterObject DocumentTypeParams params) {
        return new PagedModel<>(queryService.findAll(params));
    }

    @GetMapping("/lookup")
    public List<DocumentTypeLookup> lookup() {
        return queryService.lookup();
    }

    @GetMapping("/{id}")
    public DocumentTypeResponse findById(@PathVariable DocumentTypeId id) {
        return queryService.findById(id.value());
    }

    @PostMapping
    public ResponseEntity<DocumentTypeResponse> create(@Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(request));
    }

    @PutMapping("/{id}")
    public DocumentTypeResponse update(@PathVariable DocumentTypeId id, @Valid @RequestBody DocumentTypeRequest request) {
        return commandService.update(id.value(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable DocumentTypeId id) {
        commandService.delete(id.value());
    }

    @PatchMapping("/{id}/status")
    public DocumentTypeResponse toggleStatus(@PathVariable DocumentTypeId id) {
        return commandService.toggleStatus(id.value());
    }
}
