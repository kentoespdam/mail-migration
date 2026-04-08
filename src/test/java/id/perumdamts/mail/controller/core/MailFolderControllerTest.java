package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.folder.*;
import id.perumdamts.mail.dto.core.mail.MailComponentDto;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.folder.MailFolderCommandService;
import id.perumdamts.mail.service.core.folder.MailFolderQueryService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailFolderControllerTest {

    @Mock
    private MailFolderQueryService queryService;

    @Mock
    private MailFolderCommandService commandService;

    @Mock
    private SqidsEncoder encoder;

    private MailFolderController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailFolderController(queryService, commandService, encoder);
        principal = new MailPrincipal("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        lenient().when(encoder.decode(eq(MailFolder.class), anyString())).thenReturn(10L);
        lenient().when(encoder.decode(eq(Mail.class), anyString())).thenReturn(5L);
    }

    // ── Helper factories ──

    private MailFolderResponse sampleFolderResponse() {
        return new MailFolderResponse("1", null, "1", "INBOX", "fa-inbox", true, 5L, 10L);
    }

    private FolderCounterResponse sampleCounterResponse() {
        return new FolderCounterResponse("1", "INBOX", 5L, 10L);
    }

    private MailSummaryResponse sampleMailSummary() {
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

    // ── Tests ──

    @Test
    void getFolderTree_shouldReturnFolderList() {
        var folders = List.of(sampleFolderResponse());
        when(queryService.getFolderTree(1L)).thenReturn(folders);

        var result = controller.getFolderTree(principal);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("INBOX");
        verify(queryService).getFolderTree(1L);
    }

    @Test
    void getCounters_shouldReturnCounterList() {
        var counters = List.of(sampleCounterResponse());
        when(queryService.getCounters(1L)).thenReturn(counters);

        var result = controller.getCounters(principal);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().unread()).isEqualTo(5L);
        verify(queryService).getCounters(1L);
    }

    @Test
    void createFolder_shouldReturnCreated() {
        var request = new MailFolderRequest("My Folder", "1");
        var response = new MailFolderResponse("10", "1", "1", "My Folder", null, false, 0L, 0L);
        when(commandService.createFolder(1L, request)).thenReturn(response);

        var result = controller.createFolder(principal, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        assert result.getBody() != null;
        assertThat(result.getBody().name()).isEqualTo("My Folder");
        verify(commandService).createFolder(1L, request);
    }

    @Test
    void renameFolder_shouldReturnUpdatedFolder() {
        var request = new MailFolderRequest("Renamed Folder", "1");
        var response = new MailFolderResponse("10", "1", "1", "Renamed Folder", null, false, 0L, 0L);
        when(commandService.renameFolder(1L, 10L, request)).thenReturn(response);

        var result = controller.renameFolder(principal, "10", request);

        assertThat(result.name()).isEqualTo("Renamed Folder");
        verify(commandService).renameFolder(1L, 10L, request);
    }

    @Test
    void deleteFolder_shouldDelegateToService() {
        controller.deleteFolder(principal, "10");

        verify(commandService).deleteFolder(1L, 10L);
    }

    @Test
    void getMailsInFolder_shouldReturnMailList() {
        var params = new MailFolderMailsParams();
        var mails = List.of(sampleMailSummary());
        when(queryService.getMailsInFolder(1L, 10L, params)).thenReturn(mails);

        var result = controller.getMailsInFolder(principal, "10", params);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSubject()).isEqualTo("Test Subject");
        verify(queryService).getMailsInFolder(1L, 10L, params);
    }

    @Test
    void moveMails_shouldDelegateToService() {
        var request = new MoveMailRequest(List.of("1", "2", "3"), "1", "2");

        controller.moveMails(principal, request);

        verify(commandService).moveMails(1L, request);
    }

    @Test
    void deleteMail_shouldDelegateToService() {
        controller.deleteMail(principal, "5");

        verify(commandService).deleteMail(1L, 5L);
    }

    @Test
    void restoreMail_shouldDelegateToService() {
        controller.restoreMail(principal, "5");

        verify(commandService).restoreMail(1L, 5L);
    }

    @Test
    void emptyTrash_shouldDelegateToService() {
        controller.emptyTrash(principal);

        verify(commandService).emptyTrash(1L);
    }
}
