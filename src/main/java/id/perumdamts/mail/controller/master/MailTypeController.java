package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeParams;
import id.perumdamts.mail.dto.master.mailType.MailTypeRequest;
import id.perumdamts.mail.dto.master.mailType.MailTypeResponse;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.service.master.MailTypeService;
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
@RequestMapping("/api/v1/mail-types")
@RequiredArgsConstructor
public class MailTypeController {

    private final MailTypeService service;
    private final SqidsEncoder encoder;

    @GetMapping
    public PagedModel<MailTypeResponse> findAll(@ParameterObject MailTypeParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/lookup")
    public List<MailTypeLookup> lookup() {
        return service.lookup();
    }

    @GetMapping("/{id}")
    public MailTypeResponse findById(@PathVariable String id) {
        long rawId = encoder.decode(MailType.class, id);
        return service.findById(rawId);
    }

    @PostMapping
    public ResponseEntity<MailTypeResponse> create(@Valid @RequestBody MailTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MailTypeResponse update(@PathVariable String id, @Valid @RequestBody MailTypeRequest request) {
        long rawId = encoder.decode(MailType.class, id);
        return service.update(rawId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        long rawId = encoder.decode(MailType.class, id);
        service.delete(rawId);
    }
}
