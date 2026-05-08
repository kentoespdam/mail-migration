package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.repository.core.jpa.AttachmentRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentCommandServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private MailRepository mailRepository;
    @Mock
    private AttachmentFileStorageService storageService;
    @Mock
    private UserTaskQueryService userTaskQueryService;

    @InjectMocks
    private AttachmentCommandService commandService;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = MailPrincipal.from("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void uploadAttachment_shouldSaveAttachmentWhenAccessValid() {
        Long mailId = 123L;
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        AttachmentFileStorageService.StoredFile storedFile = new AttachmentFileStorageService.StoredFile("sys_test.pdf", "test.pdf", file.getSize());

        when(mailRepository.existsById(mailId)).thenReturn(true);
        when(userTaskQueryService.findUserTask(1L, mailId)).thenReturn(Optional.of(new UserTask()));
        when(storageService.store(file)).thenReturn(storedFile);
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attachment result = commandService.uploadAttachment(file, mailId, "notes", principal);

        assertThat(result.getOriginalFilename()).isEqualTo("test.pdf");
        assertThat(result.getSystemFilename()).isEqualTo("sys_test.pdf");
        assertThat(result.getRefId()).isEqualTo(mailId);
        assertThat(result.getDocNotes()).isEqualTo("notes");
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void deleteAttachment_shouldMarkAsDeleted() {
        Long mailId = 123L;
        Integer attachmentId = 456;
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setRefId(mailId);
        attachment.setStatus(1);

        when(mailRepository.existsById(mailId)).thenReturn(true);
        when(userTaskQueryService.findUserTask(1L, mailId)).thenReturn(Optional.of(new UserTask()));
        when(attachmentRepository.findByIdAndRefIdAndRefType(attachmentId, mailId, 1)).thenReturn(Optional.of(attachment));

        commandService.deleteAttachment(attachmentId, mailId, principal);

        assertThat(attachment.getStatus()).isEqualTo(2);
        verify(attachmentRepository).save(attachment);
    }
}
