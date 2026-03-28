package id.perumdamts.mail.mcp;

import id.perumdamts.mail.dto.core.folder.FolderCounterResponse;
import id.perumdamts.mail.dto.core.folder.MailFolderRequest;
import id.perumdamts.mail.dto.core.folder.MailFolderResponse;
import id.perumdamts.mail.dto.core.folder.MoveMailRequest;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.service.core.folder.MailFolderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderTools {

    private final MailFolderService folderService;

    public FolderTools(MailFolderService folderService) {
        this.folderService = folderService;
    }

    @Tool(description = "Get the folder tree for a user (inbox, sent, drafts, trash, custom folders)")
    public List<MailFolderResponse> getFolderTree(
            @ToolParam(description = "User ID (employee ID)") Integer userId) {
        return folderService.getFolderTree(userId);
    }

    @Tool(description = "Get unread/total counters for all folders of a user")
    public List<FolderCounterResponse> getFolderCounters(
            @ToolParam(description = "User ID (employee ID)") Integer userId) {
        return folderService.getCounters(userId);
    }

    @Tool(description = "Get mails in a specific folder with pagination")
    public List<MailSummaryResponse> getMailsInFolder(
            @ToolParam(description = "User ID (employee ID)") Integer userId,
            @ToolParam(description = "Folder ID") Integer folderId,
            @ToolParam(description = "Page number (0-based)", required = false) Integer page,
            @ToolParam(description = "Page size (default 20)", required = false) Integer size) {
        return folderService.getMailsInFolder(
                userId, folderId,
                page != null ? page : 0,
                size != null ? size : 20,
                false
        );
    }

    @Tool(description = "Create a new personal folder")
    public MailFolderResponse createFolder(
            @ToolParam(description = "User ID (employee ID)") Integer userId,
            @ToolParam(description = "Folder name") String name,
            @ToolParam(description = "Parent folder ID") Integer parentFolderId) {
        var request = new MailFolderRequest(name, parentFolderId);
        return folderService.createFolder(userId, request);
    }

    @Tool(description = "Rename a personal folder")
    public MailFolderResponse renameFolder(
            @ToolParam(description = "User ID (employee ID)") Integer userId,
            @ToolParam(description = "Folder ID to rename") Integer folderId,
            @ToolParam(description = "New folder name") String name) {
        var request = new MailFolderRequest(name, 0);
        return folderService.renameFolder(userId, folderId, request);
    }

    @Tool(description = "Delete a personal folder")
    public String deleteFolder(
            @ToolParam(description = "User ID (employee ID)") Integer userId,
            @ToolParam(description = "Folder ID to delete") Integer folderId) {
        folderService.deleteFolder(userId, folderId);
        return "Folder " + folderId + " deleted successfully";
    }

    @Tool(description = "Move mails from one folder to another")
    public String moveMails(
            @ToolParam(description = "User ID (employee ID)") Integer userId,
            @ToolParam(description = "List of mail IDs to move") List<Integer> mailIds,
            @ToolParam(description = "Source folder ID") Integer fromFolderId,
            @ToolParam(description = "Target folder ID") Integer toFolderId) {
        var request = new MoveMailRequest(mailIds, fromFolderId, toFolderId);
        folderService.moveMails(userId, request);
        return "Moved " + mailIds.size() + " mail(s) to folder " + toFolderId;
    }

    @Tool(description = "Empty the trash folder for a user, permanently deleting all trashed mails")
    public String emptyTrash(
            @ToolParam(description = "User ID (employee ID)") Integer userId) {
        int count = folderService.emptyTrash(userId);
        return "Emptied trash: " + count + " mail(s) permanently deleted";
    }
}
