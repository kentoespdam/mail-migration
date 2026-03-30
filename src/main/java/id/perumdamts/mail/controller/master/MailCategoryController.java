package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.MailCategoryParams;
import id.perumdamts.mail.dto.master.MailCategoryRequest;
import id.perumdamts.mail.dto.master.MailCategoryResponse;
import id.perumdamts.mail.infrastructure.sqids.SqidsHelper;
import id.perumdamts.mail.service.master.MailCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mail-categories")
@RequiredArgsConstructor
@Slf4j
public class MailCategoryController {

    private final MailCategoryService service;
    private final SqidsHelper sqidsHelper;

    @GetMapping
    public PagedModel<MailCategoryResponse> findAll(@ParameterObject MailCategoryParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/{id}")
    public MailCategoryResponse findById(@PathVariable String id) {
        return service.findById(sqidsHelper.decode(id));
    }

    @PostMapping
    public ResponseEntity<MailCategoryResponse> create(@Valid @RequestBody MailCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MailCategoryResponse update(@PathVariable String id, @Valid @RequestBody MailCategoryRequest request) {
        return service.update(sqidsHelper.decode(id), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(sqidsHelper.decode(id));
    }
}
