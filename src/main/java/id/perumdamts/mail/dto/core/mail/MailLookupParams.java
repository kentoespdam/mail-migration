package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.JpaPageRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MailLookupParams extends JpaPageRequest {
    private String folderId;

    @Override
    protected Set<String> allowedSorts() {
        return Set.of("mailDate", "subject", "createdByName");
    }

    @Override
    protected String defaultSort() {
        return "mailDate";
    }
}
