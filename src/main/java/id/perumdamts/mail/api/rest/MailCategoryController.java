package id.perumdamts.mail.api.rest;

import id.perumdamts.mail.api.dto.master.MailCategoryRequest;
import id.perumdamts.mail.api.dto.master.MailCategoryResponse;
import id.perumdamts.mail.service.master.MailCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mail-categories")
public class MailCategoryController {

    private final MailCategoryService service;

    public MailCategoryController(MailCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<MailCategoryResponse> findAll(
            @RequestParam(required = false) Integer mailTypeId) {
        if (mailTypeId != null) {
            return service.findByMailTypeId(mailTypeId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MailCategoryResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MailCategoryResponse> create(@Valid @RequestBody MailCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MailCategoryResponse update(@PathVariable Integer id, @Valid @RequestBody MailCategoryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
