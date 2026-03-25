package id.perumdamts.mail.api.mcp;

import id.perumdamts.mail.api.dto.master.MailCategoryResponse;
import id.perumdamts.mail.api.dto.master.MailTypeLookup;
import id.perumdamts.mail.api.dto.master.QuickMessageResponse;
import id.perumdamts.mail.service.master.MailCategoryService;
import id.perumdamts.mail.service.master.MailTypeService;
import id.perumdamts.mail.service.master.QuickMessageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterDataTools {

    private final MailTypeService mailTypeService;
    private final MailCategoryService mailCategoryService;
    private final QuickMessageService quickMessageService;

    public MasterDataTools(MailTypeService mailTypeService,
                           MailCategoryService mailCategoryService,
                           QuickMessageService quickMessageService) {
        this.mailTypeService = mailTypeService;
        this.mailCategoryService = mailCategoryService;
        this.quickMessageService = quickMessageService;
    }

    @Tool(description = "List all mail types (e.g., Surat Masuk, Surat Keluar, Nota Dinas)")
    public List<MailTypeLookup> listMailTypes() {
        return mailTypeService.lookup();
    }

    @Tool(description = "List mail categories for a specific mail type")
    public List<MailCategoryResponse> listMailCategories(
            @ToolParam(description = "Mail type ID to filter categories") Integer mailTypeId) {
        return mailCategoryService.findByMailTypeId(mailTypeId);
    }

    @Tool(description = "List available quick messages (predefined disposisi text)")
    public List<QuickMessageResponse> listQuickMessages() {
        return quickMessageService.lookup();
    }
}
