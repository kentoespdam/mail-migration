package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationFilter;
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

    public List<PublicationDto> list(PublicationFilter filter, int offset, int limit) {
        return queryRepository.findAll(filter, offset, limit);
    }

    public PublicationDto findById(Integer id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }
}
