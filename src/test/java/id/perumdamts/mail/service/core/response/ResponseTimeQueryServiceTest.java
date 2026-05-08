package id.perumdamts.mail.service.core.response;

import id.perumdamts.mail.dto.core.response.ResponseTimeAggregateDto;
import id.perumdamts.mail.dto.core.response.ResponseTimeFilterRequest;
import id.perumdamts.mail.dto.core.response.ResponseTimeStatsDto;
import id.perumdamts.mail.repository.core.jooq.ResponseTimeQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseTimeQueryServiceTest {

    @Mock
    private ResponseTimeQueryRepository responseTimeQueryRepository;

    private ResponseTimeQueryService responseTimeQueryService;

    @BeforeEach
    void setUp() {
        responseTimeQueryService = new ResponseTimeQueryService(responseTimeQueryRepository);
    }

    @Test
    void aggregate_shouldReturnStatsFromRepository() {
        ResponseTimeFilterRequest filter = new ResponseTimeFilterRequest(
                1L, 2L, 3L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        ResponseTimeStatsDto stats = new ResponseTimeStatsDto(
                100L, 3600.0, 1800.0, 5400.0, 7200.0, 60.0, 86400.0
        );
        when(responseTimeQueryRepository.aggregate(filter)).thenReturn(stats);

        ResponseTimeAggregateDto result = responseTimeQueryService.aggregate(filter);

        assertThat(result.count()).isEqualTo(100L);
        assertThat(result.avgSeconds()).isEqualTo(3600.0);
        assertThat(result.p50Seconds()).isEqualTo(1800.0);
        assertThat(result.p90Seconds()).isEqualTo(5400.0);
        assertThat(result.p99Seconds()).isEqualTo(7200.0);
    }

    @Test
    void aggregate_shouldReturnEmptyWhenNoData() {
        ResponseTimeFilterRequest filter = new ResponseTimeFilterRequest(null, null, null, null, null);
        when(responseTimeQueryRepository.aggregate(filter)).thenReturn(ResponseTimeStatsDto.empty());

        ResponseTimeAggregateDto result = responseTimeQueryService.aggregate(filter);

        assertThat(result.count()).isEqualTo(0L);
        assertThat(result.avgSeconds()).isEqualTo(0.0);
    }

    @Test
    void getDefaultStats_shouldReturnAggregatedDataWithEmptyFilter() {
        ResponseTimeStatsDto stats = new ResponseTimeStatsDto(
                500L, 1800.0, 900.0, 3600.0, 5400.0, 30.0, 43200.0
        );
        when(responseTimeQueryRepository.aggregate(ResponseTimeFilterRequest.empty())).thenReturn(stats);

        ResponseTimeAggregateDto result = responseTimeQueryService.getDefaultStats();

        assertThat(result.count()).isEqualTo(500L);
        assertThat(result.avgSeconds()).isEqualTo(1800.0);
    }

    @Test
    void aggregate_shouldFormatDuration() {
        ResponseTimeFilterRequest filter = new ResponseTimeFilterRequest(null, null, null, null, null);
        ResponseTimeStatsDto stats = new ResponseTimeStatsDto(
                10L, 3661.0, 3661.0, 3661.0, 3661.0, 60.0, 7200.0
        );
        when(responseTimeQueryRepository.aggregate(filter)).thenReturn(stats);

        ResponseTimeAggregateDto result = responseTimeQueryService.aggregate(filter);

        assertThat(result.avgFormatted()).isEqualTo("1.0h");
        assertThat(result.p50Formatted()).isEqualTo("1.0h");
        assertThat(result.p90Formatted()).isEqualTo("1.0h");
    }
}