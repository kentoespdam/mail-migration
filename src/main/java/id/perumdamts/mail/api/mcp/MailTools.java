package id.perumdamts.mail.api.mcp;

import id.perumdamts.mail.api.dto.mail.MailCreateRequest;
import id.perumdamts.mail.api.dto.mail.MailSearchRequest;
import id.perumdamts.mail.api.dto.mail.MailSummaryResponse;
import id.perumdamts.mail.api.dto.mail.MailUpdateRequest;
import id.perumdamts.mail.api.dto.mail.MailResponse;
import id.perumdamts.mail.infrastructure.security.MailPrincipal;
import id.perumdamts.mail.service.mail.MailCommandService;
import id.perumdamts.mail.service.mail.MailQueryService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailTools {

    private final MailCommandService commandService;
    private final MailQueryService queryService;

    public MailTools(MailCommandService commandService, MailQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @Tool(description = "Search mails by keyword, mail type, category, and date range")
    public List<MailSummaryResponse> searchMails(
            @ToolParam(description = "Search keyword (subject/content)") String keyword,
            @ToolParam(description = "Mail type ID filter (optional)", required = false) Integer mailTypeId,
            @ToolParam(description = "Mail category ID filter (optional)", required = false) Integer mailCategoryId,
            @ToolParam(description = "Page number (0-based)", required = false) Integer page,
            @ToolParam(description = "Page size (default 20)", required = false) Integer size) {

        var request = new MailSearchRequest(
                keyword, mailTypeId, mailCategoryId,
                null, null,
                page != null ? page : 0,
                size != null ? size : 20
        );
        return queryService.search(request);
    }

    @Tool(description = "Get the full conversation thread for a mail")
    public List<MailSummaryResponse> getMailThread(
            @ToolParam(description = "Mail ID") Integer mailId) {
        return queryService.getThread(mailId);
    }

    @Tool(description = "Create a new draft mail")
    public MailResponse createDraft(
            @ToolParam(description = "User ID (employee ID) of the sender") String userId,
            @ToolParam(description = "Mail subject") String subject,
            @ToolParam(description = "Mail content/body") String content,
            @ToolParam(description = "Mail type ID") Integer mailTypeId,
            @ToolParam(description = "Mail category ID") Integer mailCategoryId,
            @ToolParam(description = "Parent mail ID for replies (optional)", required = false) Integer parentMailId) {

        MailPrincipal principal = McpPrincipalHelper.fromUserId(userId);
        var request = new MailCreateRequest(
                subject, content, null,
                mailTypeId, mailCategoryId,
                null, null,
                null, parentMailId,
                null, null, null, null, null
        );
        return commandService.createDraft(request, principal);
    }

    @Tool(description = "Update an existing draft mail")
    public MailResponse updateDraft(
            @ToolParam(description = "User ID (employee ID)") String userId,
            @ToolParam(description = "Mail ID to update") Integer mailId,
            @ToolParam(description = "New subject (optional)", required = false) String subject,
            @ToolParam(description = "New content (optional)", required = false) String content) {

        MailPrincipal principal = McpPrincipalHelper.fromUserId(userId);
        var request = new MailUpdateRequest(
                subject, content, null,
                null, null, null, null,
                null, null, null, null, null
        );
        return commandService.updateDraft(mailId, request, principal);
    }

    @Tool(description = "Send a draft mail")
    public MailResponse sendMail(
            @ToolParam(description = "User ID (employee ID)") String userId,
            @ToolParam(description = "Mail ID to send") Integer mailId) {

        MailPrincipal principal = McpPrincipalHelper.fromUserId(userId);
        return commandService.send(mailId, principal);
    }

    @Tool(description = "Soft-delete a mail (move to trash)")
    public String deleteMail(
            @ToolParam(description = "User ID (employee ID)") String userId,
            @ToolParam(description = "Mail ID to delete") Integer mailId) {

        MailPrincipal principal = McpPrincipalHelper.fromUserId(userId);
        commandService.deleteMail(mailId, principal);
        return "Mail " + mailId + " deleted successfully";
    }

    @Tool(description = "Restore a deleted mail from trash")
    public String restoreMail(
            @ToolParam(description = "User ID (employee ID)") String userId,
            @ToolParam(description = "Mail ID to restore") Integer mailId) {

        MailPrincipal principal = McpPrincipalHelper.fromUserId(userId);
        commandService.restoreMail(mailId, principal);
        return "Mail " + mailId + " restored successfully";
    }

    @Tool(description = "Mark a mail as read")
    public String markMailAsRead(
            @ToolParam(description = "User ID (employee ID)") String userId,
            @ToolParam(description = "Mail ID to mark as read") Integer mailId) {

        MailPrincipal principal = McpPrincipalHelper.fromUserId(userId);
        commandService.markRead(mailId, principal);
        return "Mail " + mailId + " marked as read";
    }
}
