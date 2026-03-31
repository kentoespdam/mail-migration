package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PublicationQueryService {

    private final PublicationQueryRepository queryRepository;

    public PagedResponse<PublicationDto> list(PublicationParams params) {
        List<PublicationDto> items = queryRepository.findAll(params);
        long total = items.isEmpty() ? 0 : (items.getFirst().getTotalCount() != null ? items.getFirst().getTotalCount() : 0L);
        PagedResponse<PublicationDto> result = PagedResponse.of(items, params, total);
        log.debug("result {}, total {}, page {}, size {}", result.content().size(), result.totalElements(), result.page(), result.size());
        return result;
    }

    public PublicationDto findById(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }
}
