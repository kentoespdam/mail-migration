package id.perumdamts.mail.service.core.usertask;

import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.repository.core.jooq.UserTaskQueryRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTaskQueryServiceTest {

    @Mock
    private DSLContext dsl;
    @Mock
    private UserTaskQueryRepository userTaskQueryRepository;

    private UserTaskQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new UserTaskQueryService(dsl, userTaskQueryRepository);
    }

    @Test
    void findUserTask_shouldReturnUserTaskFromRepository() {
        Long userId = 1L;
        Long mailId = 123L;
        UserTask expected = new UserTask(userId, mailId, 2L);

        when(userTaskQueryRepository.findByUserIdAndMailIdAnyFolder(userId, mailId))
                .thenReturn(Optional.of(expected));

        Optional<UserTask> result = queryService.findUserTask(userId, mailId);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getMailId()).isEqualTo(mailId);
        verify(userTaskQueryRepository).findByUserIdAndMailIdAnyFolder(userId, mailId);
    }

    @Test
    void findUserTask_shouldReturnEmptyWhenNotFound() {
        Long userId = 1L;
        Long mailId = 999L;

        when(userTaskQueryRepository.findByUserIdAndMailIdAnyFolder(userId, mailId))
                .thenReturn(Optional.empty());

        Optional<UserTask> result = queryService.findUserTask(userId, mailId);

        assertThat(result).isEmpty();
    }
}