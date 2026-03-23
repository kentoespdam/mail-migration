package id.perumdamts.mail.api.rest;

import id.perumdamts.mail.api.dto.master.MailTypeRequest;
import id.perumdamts.mail.api.dto.master.MailTypeResponse;
import id.perumdamts.mail.service.master.MailTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mail-types")
@RequiredArgsConstructor
@Slf4j
public class MailTypeController {

    private final MailTypeService service;

    @GetMapping
    public List<MailTypeResponse> findAll() {
        return service.findAll();
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
