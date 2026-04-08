package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service untuk membangun thread tree dari flat mail data.
 * Implementasi dari make_nlevel_threaded() + find_node() di source PHP.
 * Fix bug di source PHP: find_node() tidak explicit return FALSE.
 */
@Service
@Transactional(readOnly = true)
public class MailThreadService {

    /**
     * Membangun n-level nested tree dari flat mail data.
     * Setiap mail punya rootMailId dan parentMailId.
     * 
     * @param flatData flat list dari database
     * @return tree structure dengan children nested
     */
    public List<MailThreadNode> buildTree(List<MailSummaryResponse> flatData) {
        if (flatData == null || flatData.isEmpty()) {
            return List.of();
        }

        // Map untuk menyimpan node by ID untuk O(1) lookup
        Map<String, MailThreadNode> nodeMap = new HashMap<>();

        // Initialize semua node
        for (MailSummaryResponse mail : flatData) {
            nodeMap.put(mail.getId(), new MailThreadNode(mail));
        }

        // Root nodes (yang parent-nya null atau diri sendiri)
        List<MailThreadNode> roots = new ArrayList<>();

        // Build tree dengan 2-level fallback strategy
        for (MailSummaryResponse mail : flatData) {
            MailThreadNode node = nodeMap.get(mail.getId());
            String parentId = mail.getThread().rootMailId() != null ? getParentId(mail) : null;

            if (parentId == null || parentId.equals(mail.getId())) {
                // Ini adalah root node
                roots.add(node);
            } else {
                // Cari parent dengan fallback strategy
                MailThreadNode parent = findNodeWithFallback(nodeMap, mail, parentId);
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    // Parent tidak ditemukan, jadikan root
                    roots.add(node);
                }
            }
        }

        return roots;
    }

    /**
     * Cari parent node dengan 2-level fallback strategy.
     * Fix bug di source PHP: tidak ada explicit return FALSE.
     * 
     * @param nodeMap  map semua node
     * @param current  node saat ini
     * @param parentId ID parent yang dicari
     * @return parent node atau null jika tidak ditemukan
     */
    private MailThreadNode findNodeWithFallback(Map<String, MailThreadNode> nodeMap,
            MailSummaryResponse current,
            String parentId) {
        // Level 1: Cari parent langsung
        MailThreadNode parent = nodeMap.get(parentId);
        if (parent != null) {
            return parent;
        }

        // Level 2: Fallback ke root mail
        if (current.getThread().rootMailId() != null && !current.getThread().rootMailId().equals(current.getId())) {
            parent = nodeMap.get(current.getThread().rootMailId());
            if (parent != null) {
                return parent;
            }
        }

        // Level 3: Fallback ke parent ID jika berbeda dari root
        if (parentId != null && !parentId.equals(current.getThread().rootMailId())) {
            parent = nodeMap.get(parentId);
            return parent;
        }

        // Tidak ditemukan - return null (fix: explicit return)
        return null;
    }

    /**
     * Get parent ID dari mail.
     * Prioritaskan parentMailId, fallback ke rootMailId.
     */
    private String getParentId(MailSummaryResponse mail) {
        if (mail.getThread().parentMailId() != null) {
            return mail.getThread().parentMailId();
        }
        return mail.getThread().rootMailId();
    }

    /**
     * Node untuk mail thread tree.
     */
    @Getter
    public static class MailThreadNode {
        private final MailSummaryResponse mail;
        private final List<MailThreadNode> children = new ArrayList<>();
        private int depth = 0;

        public MailThreadNode(MailSummaryResponse mail) {
            this.mail = mail;
        }

        public void addChild(MailThreadNode child) {
            children.add(child);
            child.depth = this.depth + 1;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }
    }
}
