package id.perumdamts.mail.mcp;

import id.perumdamts.mail.security.MailPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Helper untuk membuat {@link MailPrincipal} dari userId pada MCP tool calls.
 * MCP calls tidak melewati security filter biasa, sehingga principal
 * perlu dibuat manual dari parameter userId.
 */
final class McpPrincipalHelper {

    private McpPrincipalHelper() {}

    static MailPrincipal fromUserId(String userId) {
        return new MailPrincipal(
                userId,
                "MCP User",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
