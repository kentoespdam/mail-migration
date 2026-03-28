package id.perumdamts.mail.api.rest;

import id.perumdamts.mail.api.dto.publication.AllowedFileTypeDto;
import id.perumdamts.mail.api.dto.publication.AllowedFileTypeRequest;
import id.perumdamts.mail.service.publication.AllowedFileTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/file-rules")
public class AllowedFileTypeController {

    private final AllowedFileTypeService service;

    public AllowedFileTypeController(AllowedFileTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<AllowedFileTypeDto> listByContext(@RequestParam String context) {
        return service.listByContext(context);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AllowedFileTypeDto> listAll() {
        return service.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AllowedFileTypeDto> create(@Valid @RequestBody AllowedFileTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AllowedFileTypeDto update(@PathVariable Integer id,
                                      @Valid @RequestBody AllowedFileTypeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
