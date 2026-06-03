package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MailLookupParams extends PagedRequest {
    private String folderId;

    private static final Map<String, String> ALLOWED = Map.of(
            "mailDate", "m.m_date",
            "subject", "m.m_subject",
            "createdByName", "m.m_created_by_name"
    );

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return "m.m_date";
    }
}
