package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.recipient.MailRecipientCommandService;
import id.perumdamts.mail.service.core.recipient.MailRecipientQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailRecipientControllerTest {

    @Mock
    private MailRecipientQueryService queryService;

    @Mock
    private MailRecipientCommandService commandService;

    @Mock
    private SqidsEncoder encoder;

    private MailRecipientController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailRecipientController(queryService, commandService, encoder);
        principal = new MailPrincipal("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        lenient().when(encoder.decode(eq(Mail.class), anyString())).thenReturn(1L);
        lenient().when(encoder.decode(eq(MailRecipient.class), anyString())).thenReturn(10L);
    }

    // ── Helper factories ──

    private RecipientResponse sampleResponse() {
        return new RecipientResponse(
                "10",
                new RecipientComponentDto.EmployeeInfoDto("100", "1000", "John Doe", "Manager"),
                new RecipientComponentDto.CirculationInfoDto("1", "TO"),
                new RecipientComponentDto.NotificationInfoDto(0, 0, false));
    }

    // ── getRecipients ──

    @Test
    void getRecipients_returnsList() {
        var expected = List.of(sampleResponse());
        when(queryService.findRecipients(1L)).thenReturn(expected);

        var result = controller.getRecipients("1");

        assertThat(result).isEqualTo(expected);
        verify(queryService).findRecipients(1L);
    }

    @Test
    void getRecipients_emptyList() {
        when(queryService.findRecipients(1L)).thenReturn(List.of());

        var result = controller.getRecipients("1");

        assertThat(result).isEmpty();
    }

    // ── addRecipient ──

    @Test
    void addRecipient_returnsCreated() {
        var request = new RecipientRequest("1000", "1");
        var expected = sampleResponse();
        when(commandService.addRecipient(eq(1L), eq(request), eq(1L))).thenReturn(expected);

        var result = controller.addRecipient(principal, "1", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expected);
        verify(commandService).addRecipient(1L, request, 1L);
    }

    // ── deleteRecipient ──

    @Test
    void deleteRecipient_callsService() {
        controller.deleteRecipient(principal, "1", "10");

        verify(commandService).deleteRecipient(1L, 10L, 1L);
    }

    // ── deleteBatch ──

    @Test
    void deleteBatch_callsService() {
        var request = new RecipientDeleteBatchRequest(List.of("10", "11"));
        when(encoder.decode(eq(MailRecipient.class), eq("10"))).thenReturn(10L);
        when(encoder.decode(eq(MailRecipient.class), eq("11"))).thenReturn(11L);

        controller.deleteBatch(principal, "1", request);

        verify(commandService).deleteBatch(1L, List.of(10L, 11L), 1L);
    }

    // ── addBatch ──

    @Test
    void addBatch_returnsCreated() {
        var request = new RecipientBatchRequest(List.of("1000", "2000"), "1");
        var batchResponse = BatchRecipientResponse.of(
                List.of(sampleResponse()), List.of(), 2);
        when(commandService.addBatch(eq(1L), eq(request), eq(1L))).thenReturn(batchResponse);

        var result = controller.addBatch(principal, "1", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(batchResponse);
        verify(commandService).addBatch(1L, request, 1L);
    }

    // ── updateNotifFlags ──

    @Test
    void updateNotifFlags_returnsUpdatedResponse() {
        var request = new RecipientNotifPatchRequest(1, 0);
        var expected = sampleResponse();
        when(commandService.updateNotifFlags(eq(1L), eq(10L), eq(request), eq(1L)))
                .thenReturn(expected);
        when(encoder.decode(eq(MailRecipient.class), eq("10"))).thenReturn(10L);

        var result = controller.updateNotifFlags(principal, "1", "10", request);

        assertThat(result).isEqualTo(expected);
        verify(commandService).updateNotifFlags(1L, 10L, request, 1L);
    }

    // ── copyFrom ──

    @Test
    void copyFrom_returnsList() {
        var expected = List.of(sampleResponse());
        when(commandService.copyFrom(eq(1L), eq(2L), eq(1L))).thenReturn(expected);
        when(encoder.decode(eq(Mail.class), eq("2"))).thenReturn(2L);

        var result = controller.copyFrom(principal, "1", "2");

        assertThat(result).isEqualTo(expected);
        verify(commandService).copyFrom(1L, 2L, 1L);
    }

    // ── copyThread ──

    @Test
    void copyThread_returnsList() {
        var expected = List.of(sampleResponse());
        when(commandService.copyThread(eq(1L), eq(2L), eq(1L))).thenReturn(expected);
        when(encoder.decode(eq(Mail.class), eq("2"))).thenReturn(2L);

        var result = controller.copyThread(principal, "1", "2");

        assertThat(result).isEqualTo(expected);
        verify(commandService).copyThread(1L, 2L, 1L);
    }
}
