package id.perumdamts.mail.controller.me;

import id.perumdamts.mail.dto.id.PositionId;
import id.perumdamts.mail.dto.me.PositionResponse;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.me.UserPositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPositionControllerTest {

    @Mock
    private UserPositionService service;

    private UserPositionController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new UserPositionController(service);
        principal = MailPrincipal.from("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getPositions_shouldReturnPositionList() {
        var positions = List.of(
                new PositionResponse(new PositionId(10L), "Manager", "IT Division", false, true)
        );
        when(service.getPositionsForUser(principal)).thenReturn(positions);

        var result = controller.getPositions(principal);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPosId()).isEqualTo(new PositionId(10L));
        assertThat(result.getFirst().isActive()).isTrue();
        assertThat(result.getFirst().isPlt()).isFalse();
    }

    @Test
    void getPositions_shouldReturnPltPosition() {
        var positions = List.of(
                new PositionResponse(new PositionId(10L), "Manager", "IT Division", true, false)
        );
        when(service.getPositionsForUser(principal)).thenReturn(positions);

        var result = controller.getPositions(principal);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isPlt()).isTrue();
    }
}
