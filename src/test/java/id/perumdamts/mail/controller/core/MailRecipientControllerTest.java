package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.recipient.*;
import id.perumdamts.mail.dto.id.CirculationTypeId;
import id.perumdamts.mail.dto.id.EmployeeId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.id.MailRecipientId;
import id.perumdamts.mail.dto.id.UserId;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.recipient.MailRecipientCommandService;
import id.perumdamts.mail.service.core.recipient.MailRecipientQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailRecipientControllerTest {

    @Mock
    private MailRecipientQueryService queryService;

    @Mock
    private MailRecipientCommandService commandService;

    private MailRecipientController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailRecipientController(queryService, commandService);
        principal = MailPrincipal.from("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ── Helper factories ──

    private RecipientResponse sampleResponse() {
        return new RecipientResponse(
                new MailRecipientId(10L),
                new RecipientComponentDto.EmployeeInfoDto(new UserId(100L), new EmployeeId(1000L), "John Doe", "Manager"),
                new RecipientComponentDto.CirculationInfoDto("1", "TO"),
                new RecipientComponentDto.NotificationInfoDto(0, 0, false));
    }

    // ── getRecipients ──

    @Test
    void getRecipients_returnsList() {
        var expected = List.of(sampleResponse());
        when(queryService.findRecipients(1L)).thenReturn(expected);

        var result = controller.getRecipients(new MailId(1L));

        assertThat(result).isEqualTo(expected);
        verify(queryService).findRecipients(1L);
    }

    @Test
    void getRecipients_emptyList() {
        when(queryService.findRecipients(1L)).thenReturn(List.of());

        var result = controller.getRecipients(new MailId(1L));

        assertThat(result).isEmpty();
    }

    // ── addRecipient ──

    @Test
    void addRecipient_returnsCreated() {
        var request = new RecipientRequest(new UserId(1000L), new CirculationTypeId(1L));
        var expected = sampleResponse();
        when(commandService.addRecipient(eq(1L), eq(request), eq(1L))).thenReturn(expected);

        var result = controller.addRecipient(principal, new MailId(1L), request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expected);
        verify(commandService).addRecipient(1L, request, 1L);
    }

    // ── deleteRecipient ──

    @Test
    void deleteRecipient_callsService() {
        controller.deleteRecipient(principal, new MailId(1L), new MailRecipientId(10L));

        verify(commandService).deleteRecipient(1L, 10L, 1L);
    }

    // ── deleteBatch ──

    @Test
    void deleteBatch_callsService() {
        var request = new RecipientDeleteBatchRequest(List.of(new MailRecipientId(10L), new MailRecipientId(11L)));

        controller.deleteBatch(principal, new MailId(1L), request);

        verify(commandService).deleteBatch(1L, List.of(10L, 11L), 1L);
    }

    // ── addBatch ──

    @Test
    void addBatch_returnsCreated() {
        var request = new RecipientBatchRequest(List.of(new UserId(1000L), new UserId(2000L)), new CirculationTypeId(1L));
        var batchResponse = BatchRecipientResponse.of(
                List.of(sampleResponse()), List.of(), 2);
        when(commandService.addBatch(eq(1L), eq(request), eq(1L))).thenReturn(batchResponse);

        var result = controller.addBatch(principal, new MailId(1L), request);

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

        var result = controller.updateNotifFlags(principal, new MailId(1L), new MailRecipientId(10L), request);

        assertThat(result).isEqualTo(expected);
        verify(commandService).updateNotifFlags(1L, 10L, request, 1L);
    }

    // ── copyFrom ──

    @Test
    void copyFrom_returnsList() {
        var expected = List.of(sampleResponse());
        when(commandService.copyFrom(eq(1L), eq(2L), eq(1L))).thenReturn(expected);

        var result = controller.copyFrom(principal, new MailId(1L), new MailId(2L));

        assertThat(result).isEqualTo(expected);
        verify(commandService).copyFrom(1L, 2L, 1L);
    }

    // ── copyThread ──

    @Test
    void copyThread_returnsList() {
        var expected = List.of(sampleResponse());
        when(commandService.copyThread(eq(1L), eq(2L), eq(1L))).thenReturn(expected);

        var result = controller.copyThread(principal, new MailId(1L), new MailId(2L));

        assertThat(result).isEqualTo(expected);
        verify(commandService).copyThread(1L, 2L, 1L);
    }
}
