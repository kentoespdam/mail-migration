package id.perumdamts.mail.api.mcp;

import id.perumdamts.mail.api.dto.recipient.BatchRecipientResponse;
import id.perumdamts.mail.api.dto.recipient.RecipientBatchRequest;
import id.perumdamts.mail.api.dto.recipient.RecipientRequest;
import id.perumdamts.mail.api.dto.recipient.RecipientResponse;
import id.perumdamts.mail.service.recipient.MailRecipientService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipientTools {

    private final MailRecipientService recipientService;

    public RecipientTools(MailRecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @Tool(description = "Get the list of recipients for a mail")
    public List<RecipientResponse> getRecipients(
            @ToolParam(description = "Mail ID") Integer mailId) {
        return recipientService.getRecipients(mailId);
    }

    @Tool(description = "Add a single recipient to a mail. Circulation types: 1=TO, 2=CC, 3=REPLY")
    public RecipientResponse addRecipient(
            @ToolParam(description = "Mail ID") Integer mailId,
            @ToolParam(description = "Employee ID of the recipient") Integer employeeId,
            @ToolParam(description = "Circulation type: 1=TO, 2=CC, 3=REPLY") Integer circulationType) {
        var request = new RecipientRequest(employeeId, circulationType);
        return recipientService.addRecipient(mailId, request);
    }

    @Tool(description = "Add multiple recipients to a mail in batch")
    public BatchRecipientResponse addRecipientsBatch(
            @ToolParam(description = "Mail ID") Integer mailId,
            @ToolParam(description = "List of employee IDs") List<Integer> employeeIds,
            @ToolParam(description = "Circulation type: 1=TO, 2=CC, 3=REPLY") Integer circulationType) {
        var request = new RecipientBatchRequest(employeeIds, circulationType);
        return recipientService.addBatch(mailId, request);
    }

    @Tool(description = "Remove a recipient from a mail")
    public String deleteRecipient(
            @ToolParam(description = "Mail ID") Integer mailId,
            @ToolParam(description = "Recipient ID to remove") Long recipientId) {
        recipientService.deleteRecipient(mailId, recipientId);
        return "Recipient " + recipientId + " removed from mail " + mailId;
    }
}
