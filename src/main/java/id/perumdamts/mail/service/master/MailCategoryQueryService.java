package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.mailCategory.MailCategoryParams;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.repository.master.jooq.MailCategoryQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailCategoryQueryService {
    private final MailCategoryQueryRepository repository;

    public Page<MailCategoryResponse> findAll(MailCategoryParams params) {
        return repository.findAll(params);
    }

    public MailCategoryResponse findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailCategory not found: " + id));
    }
}
