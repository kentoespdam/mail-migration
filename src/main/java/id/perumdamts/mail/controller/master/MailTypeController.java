package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.MailTypeLookup;
import id.perumdamts.mail.dto.master.MailTypeRequest;
import id.perumdamts.mail.dto.master.MailTypeResponse;
import id.perumdamts.mail.service.master.MailTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mail-types")
@RequiredArgsConstructor
public class MailTypeController {

    private final MailTypeService service;

    @GetMapping
    public Page<MailTypeResponse> findAll(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return service.findAll(search, pageable);
    }

    @GetMapping("/lookup")
    public List<MailTypeLookup> lookup() {
        return service.lookup();
    }

    @GetMapping("/{id}")
    public MailTypeResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MailTypeResponse> create(@Valid @RequestBody MailTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MailTypeResponse update(@PathVariable Integer id, @Valid @RequestBody MailTypeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
