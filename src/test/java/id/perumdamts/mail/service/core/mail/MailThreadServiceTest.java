package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.core.mail.MailComponentDto;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
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
        MailSummaryResponse root = createSummary(1L, null, null);
        MailSummaryResponse child1 = createSummary(2L, 1L, 1L);
        MailSummaryResponse child2 = createSummary(3L, 1L, 1L);
        MailSummaryResponse grandchild = createSummary(4L, 1L, 2L);

        List<MailSummaryResponse> flatData = List.of(root, child1, child2, grandchild);

        // When
        List<MailThreadService.MailThreadNode> tree = service.buildTree(flatData);

        // Then
        assertThat(tree).hasSize(1);
        MailThreadService.MailThreadNode rootNode = tree.getFirst();
        assertThat(rootNode.getMail().id()).isEqualTo(new MailId(1L));
        assertThat(rootNode.getChildren()).hasSize(2);

        MailThreadService.MailThreadNode child1Node = rootNode.getChildren().stream()
                .filter(n -> n.getMail().id().equals(new MailId(2L)))
                .findFirst().orElseThrow();
        assertThat(child1Node.getChildren()).hasSize(1);
        assertThat(child1Node.getChildren().getFirst().getMail().id()).isEqualTo(new MailId(4L));
        assertThat(child1Node.getDepth()).isEqualTo(1);
        assertThat(child1Node.getChildren().getFirst().getDepth()).isEqualTo(2);
    }

    @Test
    void buildTree_shouldHandleNullOrEmptyData() {
        assertThat(service.buildTree(null)).isEmpty();
        assertThat(service.buildTree(List.of())).isEmpty();
    }

    private MailSummaryResponse createSummary(Long id, Long rootId, Long parentId) {
        return new MailSummaryResponse(
                new MailId(id), "NO-" + id, LocalDate.now(), "Subject " + id,
                new MailComponentDto.MailAuditInfoDto(null, "Creator", LocalDateTime.now(), null),
                new MailComponentDto.MailSummaryInfoDto(0, "Recipient"),
                0, new MailFolderId(1L),
                new MailTypeLookup(null, "Type"),
                new MailCategoryLookup(null, "Category"),
                null,
                new MailFolderLookup(null, null),
                new MailComponentDto.MailThreadInfoDto(rootId != null ? new MailId(rootId) : null, parentId != null ? new MailId(parentId) : null),
                1L);
    }
}
