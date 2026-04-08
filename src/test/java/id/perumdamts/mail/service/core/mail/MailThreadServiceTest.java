package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MailThreadServiceTest {

    private final MailThreadService service = new MailThreadService();

    @Test
    void buildTree_shouldConstructCorrectHierarchy() {
        // Given
        MailSummaryResponse root = createSummary("root", null, null);
        MailSummaryResponse child1 = createSummary("child1", "root", "root");
        MailSummaryResponse child2 = createSummary("child2", "root", "root");
        MailSummaryResponse grandchild = createSummary("grandchild", "root", "child1");

        List<MailSummaryResponse> flatData = List.of(root, child1, child2, grandchild);

        // When
        List<MailThreadService.MailThreadNode> tree = service.buildTree(flatData);

        // Then
        assertThat(tree).hasSize(1);
        MailThreadService.MailThreadNode rootNode = tree.getFirst();
        assertThat(rootNode.getMail().id()).isEqualTo("root");
        assertThat(rootNode.getChildren()).hasSize(2);

        MailThreadService.MailThreadNode child1Node = rootNode.getChildren().stream()
                .filter(n -> n.getMail().id().equals("child1"))
                .findFirst().orElseThrow();
        assertThat(child1Node.getChildren()).hasSize(1);
        assertThat(child1Node.getChildren().getFirst().getMail().id()).isEqualTo("grandchild");
        assertThat(child1Node.getDepth()).isEqualTo(1);
        assertThat(child1Node.getChildren().getFirst().getDepth()).isEqualTo(2);
    }

    @Test
    void buildTree_shouldHandleNullOrEmptyData() {
        assertThat(service.buildTree(null)).isEmpty();
        assertThat(service.buildTree(List.of())).isEmpty();
    }

    private MailSummaryResponse createSummary(String id, String rootId, String parentId) {
        return new MailSummaryResponse(
                id, "NO-" + id, LocalDate.now(), "Subject " + id,
                "Creator", "Recipient", 0, "1", 0, LocalDateTime.now(),
                "Type", "Category", null, null, null,
                rootId, parentId, 1L);
    }
}
