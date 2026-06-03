package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.id.AllowedFileTypeId;
import id.perumdamts.mail.dto.master.allowedFileType.AllowedFileTypeDto;
import id.perumdamts.mail.dto.master.allowedFileType.AllowedFileTypeParams;
import id.perumdamts.mail.dto.master.allowedFileType.AllowedFileTypeRequest;
import id.perumdamts.mail.service.master.allowedFileType.AllowedFileTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/file-rules")
@RequiredArgsConstructor
public class AllowedFileTypeController {

    private final AllowedFileTypeService service;

    @GetMapping
    public PagedModel<AllowedFileTypeDto> findAll(@ParameterObject AllowedFileTypeParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/lookup")
    public List<AllowedFileTypeDto> listByContext(@RequestParam String context) {
        return service.listByContext(context);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AllowedFileTypeDto> create(@Valid @RequestBody AllowedFileTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AllowedFileTypeDto update(@PathVariable AllowedFileTypeId id,
                                      @Valid @RequestBody AllowedFileTypeRequest request) {
        return service.update(id.value(), request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable AllowedFileTypeId id) {
        service.delete(id.value());
    }
}
