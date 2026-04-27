package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.publication.PublicationCommandResult;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.publication.PublicationCommandHandler;
import id.perumdamts.mail.service.core.publication.PublicationQueryHandler;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationCommandHandler commandHandler;

    @Mock
    private PublicationQueryHandler queryHandler;

    @Mock
    private SqidsEncoder encoder;

    private PublicationController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new PublicationController(commandHandler, queryHandler, encoder);
        principal = new MailPrincipal("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private PublicationResponse sampleResponse(String status) {
        return new PublicationResponse(
                "1", "Test Publication", "Description",
                new DocumentTypeLookup("1", "Internal"),
                status, LocalDateTime.now(), "file.pdf", 1024,
                "Test User", "Staff", 1,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void publish_shouldReturnPublishedResponse() {
        var response = sampleResponse("PUBLISHED");
        var resultMeta = new PublicationCommandResult("1", "PUBLISHED", LocalDateTime.now());
        when(encoder.decode(Publication.class, "1")).thenReturn(1L);
        when(commandHandler.publish(1L, principal)).thenReturn(resultMeta);
        when(queryHandler.findById(1L)).thenReturn(response);

        var result = controller.publish(principal, "1");

        assertThat(result).isEqualTo(response);
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void publish_shouldThrowException_whenServiceThrows() {
        when(encoder.decode(Publication.class, "1")).thenReturn(1L);
        when(commandHandler.publish(1L, principal)).thenThrow(new IllegalStateException("Already published"));

        assertThatThrownBy(() -> controller.publish(principal, "1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Already published");
    }
}
