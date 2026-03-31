package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.mailCategory.MailCategoryParams;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryRequest;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.service.master.MailCategoryService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mail-categories")
@RequiredArgsConstructor
public class MailCategoryController {

    private final MailCategoryService service;
    private final SqidsEncoder encoder;

    @GetMapping
    public PagedModel<MailCategoryResponse> findAll(@ParameterObject MailCategoryParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/{id}")
    public MailCategoryResponse findById(@PathVariable String id) {
        long rawId = encoder.decode(MailCategory.class, id);
        return service.findById(rawId);
    }

    @PostMapping
    public ResponseEntity<MailCategoryResponse> create(@Valid @RequestBody MailCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MailCategoryResponse update(@PathVariable String id, @Valid @RequestBody MailCategoryRequest request) {
        long rawId = encoder.decode(MailCategory.class, id);
        return service.update(rawId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        long rawId = encoder.decode(MailCategory.class, id);
        service.delete(rawId);
    }
}
