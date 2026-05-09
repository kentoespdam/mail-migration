package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailReportRequest;
import id.perumdamts.mail.dto.core.mail.MailReportResponse;
import id.perumdamts.mail.repository.core.jooq.AttachmentQueryRepository;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailQueryServiceTest {

    @Mock
    private MailQueryRepository mailQueryRepository;

    @Mock
    private AttachmentQueryRepository attachmentQueryRepository;

    @Mock
    private UserTaskQueryService userTaskQueryService;

    private MailQueryService mailQueryService;

    @BeforeEach
    void setUp() {
        mailQueryService = new MailQueryService(mailQueryRepository, attachmentQueryRepository, userTaskQueryService);
    }

    @Test
    void findReport_shouldApplyDefaultDateRangeWhenNull() {
        // Arrange
        MailReportRequest request = new MailReportRequest();
        request.setStartDate(null);
        request.setEndDate(null);

        MailReportResponse reportItem = new MailReportResponse("Type", "Category", 10, 5, 5, 1);
        when(mailQueryRepository.getReport(request)).thenReturn(List.of(reportItem));

        // Act
        mailQueryService.findReport(request);

        // Assert
        ArgumentCaptor<MailReportRequest> captor = ArgumentCaptor.forClass(MailReportRequest.class);
        verify(mailQueryRepository).getReport(captor.capture());
        
        MailReportRequest capturedRequest = captor.getValue();
        LocalDate now = LocalDate.now();
        assertThat(capturedRequest.getStartDate()).isEqualTo(now.with(TemporalAdjusters.firstDayOfMonth()));
        assertThat(capturedRequest.getEndDate()).isEqualTo(now.with(TemporalAdjusters.lastDayOfMonth()));
    }

    @Test
    void findReport_shouldUseProvidedDateRange() {
        // Arrange
        MailReportRequest request = new MailReportRequest();
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        MailReportResponse reportItem = new MailReportResponse("Type", "Category", 10, 5, 5, 1);
        when(mailQueryRepository.getReport(request)).thenReturn(List.of(reportItem));

        // Act
        mailQueryService.findReport(request);

        // Assert
        ArgumentCaptor<MailReportRequest> captor = ArgumentCaptor.forClass(MailReportRequest.class);
        verify(mailQueryRepository).getReport(captor.capture());
        
        MailReportRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getStartDate()).isEqualTo(startDate);
        assertThat(capturedRequest.getEndDate()).isEqualTo(endDate);
    }
}
