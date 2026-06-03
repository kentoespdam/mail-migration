package id.perumdamts.mail.service.core.response;

import id.perumdamts.mail.dto.core.response.ResponseTimeAggregateDto;
import id.perumdamts.mail.dto.core.response.ResponseTimeFilterRequest;
import id.perumdamts.mail.dto.core.response.ResponseTimeStatsDto;
import id.perumdamts.mail.repository.core.jooq.ResponseTimeQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseTimeQueryService {

    private final ResponseTimeQueryRepository responseTimeQueryRepository;

    public ResponseTimeAggregateDto aggregate(ResponseTimeFilterRequest filter) {
        ResponseTimeStatsDto stats = responseTimeQueryRepository.aggregate(filter);
        return ResponseTimeAggregateDto.fromStats(stats);
    }

    public ResponseTimeAggregateDto getDefaultStats() {
        return aggregate(ResponseTimeFilterRequest.empty());
    }
}