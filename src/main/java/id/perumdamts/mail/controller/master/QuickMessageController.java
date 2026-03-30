package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.QuickMessageParams;
import id.perumdamts.mail.dto.master.QuickMessageRequest;
import id.perumdamts.mail.dto.master.QuickMessageResponse;
import id.perumdamts.mail.service.master.QuickMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quick-messages")
@RequiredArgsConstructor
public class QuickMessageController {
    private final QuickMessageService service;

    /**
     * Lookup — semua pesan ACTIVE untuk dropdown/autocomplete di compose mail.
     */
    @GetMapping("/lookup")
    public List<QuickMessageResponse> lookup() {
        return service.lookup();
    }

    /**
     * Paginated list — untuk admin panel (termasuk INACTIVE).
     */
    @GetMapping
    public Page<QuickMessageResponse> findAll(@ParameterObject QuickMessageParams params) {
        return service.findAll(params);
    }

    @GetMapping("/{id}")
    public QuickMessageResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<QuickMessageResponse> create(@Valid @RequestBody QuickMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public QuickMessageResponse update(@PathVariable Integer id, @Valid @RequestBody QuickMessageRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
