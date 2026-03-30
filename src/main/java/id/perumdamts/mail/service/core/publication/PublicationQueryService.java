package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicationQueryService {

    private final PublicationQueryRepository queryRepository;

    public PublicationQueryService(PublicationQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public PagedResponse<PublicationDto> list(PublicationParams params) {
        List<PublicationDto> items = queryRepository.findAll(params);
        long total = items.isEmpty() ? 0 : items.getFirst().totalCount();
        return PagedResponse.of(items, params, total);
    }

    public PublicationDto findById(Integer id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }
}
