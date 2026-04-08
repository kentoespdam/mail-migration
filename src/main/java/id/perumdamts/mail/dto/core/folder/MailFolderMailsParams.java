package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class MailFolderMailsParams extends PagedRequest {

    private static final Map<String, String> ALLOWED = Map.of(
            "createdDate", "m.m_created_date",
            "mailDate", "m.m_date",
            "subject", "m.m_subject",
            "mailNumber", "m.m_no"
    );
    private static final String DEFAULT = "m.m_created_date";

    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate sdate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate edate;

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return DEFAULT;
    }
}
