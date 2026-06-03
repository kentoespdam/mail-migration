package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.dto.id.*;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.DispositionStatusDeriver;
import id.perumdamts.mail.service.core.mail.MailCommandService;
import id.perumdamts.mail.service.core.mail.MailQueryService;
import id.perumdamts.mail.service.core.recipient.MailRecipientQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private MailRecipientQueryService recipientQueryService;

    @Mock
    private DispositionStatusDeriver dispositionStatusDeriver;

    private MailController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailController(commandService, queryService, recipientQueryService, dispositionStatusDeriver);
        principal = MailPrincipal.from("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ── Helper factories ──

    private MailResponse sampleMailResponse() {
        return new MailResponse(
                new MailId(1L), "001/2025", LocalDate.of(2025, 1, 1),
                new MailTypeLookup(new MailTypeId(1L), "Surat Masuk"),
                new MailCategoryLookup(new MailCategoryId(1L), "Umum"),
                "Test Subject", "content", "note",
                LocalDate.of(2025, 1, 15), 1,
                new MailComponentDto.MailThreadInfoDto(null, null),
                new MailComponentDto.MailSummaryInfoDto(0, "Recipient"),
                new MailComponentDto.MailAuditInfoDto(new UserId(1L), "Test User", LocalDateTime.now(), LocalDateTime.now()),
                null,
                null,
                null,
                null,
                null,
                java.util.Collections.emptyList());
    }

    private MailSummaryResponse sampleSummary() {
        return new MailSummaryResponse(
                new MailId(1L), "001/2025", LocalDate.of(2025, 1, 1),
                "Test Subject",
                new MailComponentDto.MailAuditInfoDto(null, "Test User", LocalDateTime.now(), null),
                new MailComponentDto.MailSummaryInfoDto(0, "Recipient"),
                0, new MailFolderId(1L),
                new MailTypeLookup(new MailTypeId(1L), "Surat Masuk"),
                new MailCategoryLookup(new MailCategoryId(1L), "Umum"),
                null,
                new MailFolderLookup(null, null),
                new MailComponentDto.MailThreadInfoDto(null, null),
                1L);
    }

    private MailCreateRequest sampleCreateRequest() {
        return new MailCreateRequest(
                "Test Subject", "content", "note",
                new MailTypeId(1L), new MailCategoryId(1L), LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 15),
                null, null, null, null, null, null, null);
    }

    private MailUpdateRequest sampleUpdateRequest() {
        return new MailUpdateRequest(
                "Updated Subject", "new content", "new note",
                new MailTypeId(1L), new MailCategoryId(1L), LocalDate.of(2025, 1, 1),
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
                new MailTypeId(1L), new MailCategoryId(1L), LocalDate.of(2025, 1, 1),
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
        var mailId = new MailId(1L);
        when(commandService.updateDraft(eq(1L), eq(request), eq(principal))).thenReturn(response);

        var result = controller.updateDraft(principal, mailId, request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void send_shouldReturnResponse() {
        var response = sampleMailResponse();
        var mailId = new MailId(1L);
        when(commandService.send(1L, principal)).thenReturn(response);

        var result = controller.send(principal, mailId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void deleteMail_shouldDelegateToService() {
        var mailId = new MailId(1L);
        controller.deleteMail(principal, mailId);

        verify(commandService).deleteMail(1L, principal);
    }

    @Test
    void restoreMail_shouldDelegateToService() {
        var mailId = new MailId(1L);
        controller.restoreMail(principal, mailId);

        verify(commandService).restoreMail(1L, principal);
    }

    @Test
    void markRead_shouldDelegateToService() {
        var mailId = new MailId(1L);
        controller.markRead(principal, mailId);

        verify(commandService).markRead(1L, principal);
    }

    @Test
    void getThread_shouldReturnList() {
        var summaries = List.of(sampleSummary());
        var mailId = new MailId(1L);
        when(queryService.findThread(1L)).thenReturn(summaries);

        var result = controller.getThread(mailId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(mailId);
    }

    @Test
    void getById_shouldReturnResponse() {
        var response = sampleMailResponse();
        var mailId = new MailId(1L);
        when(queryService.getDetail(eq(1L), anyLong())).thenReturn(response);

        var result = controller.getById(principal, mailId);

        assertThat(result).isEqualTo(response);
        verify(queryService).getDetail(eq(1L), eq(principal.userIdLong()));
    }

    @Test
    void getTracking_shouldReturnList() {
        var tracking = List.of(
                MailTrackingResponse.root(new MailRecipientId(1L), "Emp", "Pos", "MEMO", false, null),
                MailTrackingResponse.child(new MailRecipientId(2L), "Emp2", "Pos2", "DISPOSISI", true, null, 1)
        );
        var mailId = new MailId(1L);
        when(recipientQueryService.findTracking(1L)).thenReturn(tracking);

        var result = controller.getTracking(mailId);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().recipientId()).isEqualTo(new MailRecipientId(1L));
        assertThat(result.getFirst().isRoot()).isTrue();
        assertThat(result.get(1).isRoot()).isFalse();
        assertThat(result.get(1).depth()).isEqualTo(1);
    }

    @Test
    void getReadStatus_shouldReturnList() {
        var status = List.of(new RecipientReadStatusResponse(new MailRecipientId(1L), new UserId(1L), "Emp", "Pos", "MEMO", 0, null));
        var mailId = new MailId(1L);
        when(recipientQueryService.findReadStatus(1L)).thenReturn(status);

        var result = controller.getReadStatus(mailId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().recipientId()).isEqualTo(new MailRecipientId(1L));
    }

    @Test
    void search_shouldReturnPagedModel() {
        var request = new MailSearchRequest();
        request.setKeyword("test");
        var page = new PageImpl<>(List.of(sampleSummary()), PageRequest.of(0, 20), 1L);
        when(queryService.search(request)).thenReturn(page);

        var result = controller.search(request);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getMetadata().totalElements()).isEqualTo(1L);
    }

    @Test
    void lookup_shouldReturnPagedModel() {
        var params = new MailLookupParams();
        params.setPage(0);
        params.setSize(20);
        var lookupItem = new MailLookupResponse(
                new MailId(1L), LocalDate.of(2025, 1, 1), "Test User",
                "Test Subject", "Surat Masuk", "Umum",
                "DISPOSISI", LocalDate.of(2025, 1, 15), false);
        var page = new PageImpl<>(List.of(lookupItem), PageRequest.of(0, 20), 1L);
        when(queryService.findLookup(principal.userIdLong(), params)).thenReturn(page);

        var result = controller.lookup(principal, params);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getMetadata().totalElements()).isEqualTo(1L);
    }

    @Test
    void report_shouldReturnPagedModel() {
        var request = new MailReportRequest();
        var reportItem = new MailReportResponse("Surat Masuk", "Umum", 10, 8, 2, 1);
        var page = new PageImpl<>(List.of(reportItem), PageRequest.of(0, 20), 1L);
        when(queryService.findReport(request)).thenReturn(page);

        var result = controller.report(request);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().totalMails()).isEqualTo(10);
    }
}
