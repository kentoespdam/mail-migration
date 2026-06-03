package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.id.MailCategoryId;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryParams;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryRequest;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.service.master.mailCategory.MailCategoryCommandService;
import id.perumdamts.mail.service.master.mailCategory.MailCategoryQueryService;
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

    private final MailCategoryCommandService commandService;
    private final MailCategoryQueryService queryService;

    @GetMapping
    public PagedModel<MailCategoryResponse> findAll(@ParameterObject MailCategoryParams params) {
        return new PagedModel<>(queryService.findAll(params));
    }

    @GetMapping("/{id}")
    public MailCategoryResponse findById(@PathVariable MailCategoryId id) {
        return queryService.findById(id.value());
    }

    @PostMapping
    public ResponseEntity<MailCategoryResponse> create(@Valid @RequestBody MailCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(request));
    }

    @PutMapping("/{id}")
    public MailCategoryResponse update(@PathVariable MailCategoryId id, @Valid @RequestBody MailCategoryRequest request) {
        return commandService.update(id.value(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MailCategoryId id) {
        commandService.delete(id.value());
    }
}

