package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailCommandService;
import id.perumdamts.mail.service.core.mail.MailQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailControllerTest {

    @Mock
    private MailCommandService commandService;

    @Mock
    private MailQueryService queryService;

    @Mock
    private SqidsEncoder encoder;

    private MailController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailController(commandService, queryService, encoder);
        principal = new MailPrincipal("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ── Helper factories ──

    private MailResponse sampleMailResponse() {
        return new MailResponse(
                "1", "001/2025", LocalDate.of(2025, 1, 1),
                new MailTypeLookup("1", "Surat Masuk"),
                new MailCategoryLookup("1", "Umum"),
                "Test Subject", "content", "note",
                LocalDate.of(2025, 1, 15), 1,
                new MailComponentDto.MailThreadInfoDto(null, null),
                new MailComponentDto.MailSummaryInfoDto(0, "Recipient"),
                new MailComponentDto.MailAuditInfoDto("1", "Test User", LocalDateTime.now(), LocalDateTime.now()),
                null, null, null, null, null);
    }

    private MailSummaryResponse sampleSummary() {
        return new MailSummaryResponse(
                "1", "001/2025", LocalDate.of(2025, 1, 1),
                "Test Subject",
                new MailComponentDto.MailAuditInfoDto(null, "Test User", LocalDateTime.now(), null),
                new MailComponentDto.MailSummaryInfoDto(0, "Recipient"),
                0, "1",
                new MailTypeLookup(null, "Surat Masuk"),
                new MailCategoryLookup(null, "Umum"),
                null,
                new MailFolderLookup(null, null),
                new MailComponentDto.MailThreadInfoDto(null, null),
                1L);
    }

    private MailCreateRequest sampleCreateRequest() {
        return new MailCreateRequest(
                "Test Subject", "content", "note",
                "1", "1", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 15),
                null, null, null, null, null, null, null);
    }

    private MailUpdateRequest sampleUpdateRequest() {
        return new MailUpdateRequest(
                "Updated Subject", "new content", "new note",
                "1", "1", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 15),
                null, null, null, null, null, null, null);
    }

    // ── Tests ──

    @Test
    void createDraft_shouldReturnCreated() {
        var request = sampleCreateRequest();
        var response = sampleMailResponse();
        when(commandService.createDraft(request, principal)).thenReturn(response);

        var result = controller.createDraft(principal, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(commandService).createDraft(request, principal);
    }

    @Test
    void sendMail_shouldReturnCreated() {
        var request = new MailSendRequest(
                "Subject", "content", "note",
                "1", "1", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 15),
                null, null, null, null, null, null, null,
                List.of());
        var response = sampleMailResponse();
        when(commandService.sendMail(request, principal)).thenReturn(response);

        var result = controller.sendMail(principal, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void updateDraft_shouldReturnResponse() {
        var request = sampleUpdateRequest();
        var response = sampleMailResponse();
        when(encoder.decode(Mail.class, "1")).thenReturn(1L);
        when(commandService.updateDraft(eq(1L), eq(request), eq(principal))).thenReturn(response);

        var result = controller.updateDraft(principal, "1", request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void send_shouldReturnResponse() {
        var response = sampleMailResponse();
        when(encoder.decode(Mail.class, "1")).thenReturn(1L);
        when(commandService.send(1L, principal)).thenReturn(response);

        var result = controller.send(principal, "1");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void deleteMail_shouldDelegateToService() {
        when(encoder.decode(Mail.class, "1")).thenReturn(1L);
        controller.deleteMail(principal, "1");

        verify(commandService).deleteMail(1L, principal);
    }

    @Test
    void restoreMail_shouldDelegateToService() {
        when(encoder.decode(Mail.class, "1")).thenReturn(1L);
        controller.restoreMail(principal, "1");

        verify(commandService).restoreMail(1L, principal);
    }

    @Test
    void markRead_shouldDelegateToService() {
        when(encoder.decode(Mail.class, "1")).thenReturn(1L);
        controller.markRead(principal, "1");

        verify(commandService).markRead(1L, principal);
    }

    @Test
    void getThread_shouldReturnList() {
        var summaries = List.of(sampleSummary());
        when(encoder.decode(Mail.class, "1")).thenReturn(1L);
        when(queryService.getThread(1L)).thenReturn(summaries);

        var result = controller.getThread("1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("1");
    }

    @Test
    void search_shouldReturnPagedResponse() {
        var request = new MailSearchRequest();
        request.setKeyword("test");
        var paged = PagedResponse.of(List.of(sampleSummary()), 0, 20, 1L);
        when(queryService.search(request)).thenReturn(paged);

        var result = controller.search(request);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    void report_shouldReturnPagedResponse() {
        var request = new MailReportRequest();
        var reportItem = new MailReportResponse("Surat Masuk", "Umum", 10, 8, 2, 1);
        var paged = PagedResponse.of(List.of(reportItem), 0, 20, 1L);
        when(queryService.getReport(request)).thenReturn(paged);

        var result = controller.report(request);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().getTotalMails()).isEqualTo(10);
    }
}
