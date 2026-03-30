package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class MailReportRequest extends PagedRequest {

    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "mailTypeName",     "mt.mail_type",
            "mailCategoryName", "mc.mcat_name",
            "totalMails",       "totalMails"
    );
    private static final String DEFAULT_SORT = "mt.mail_type";

    private Integer mailTypeId;
    private Integer mailCategoryId;
    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    protected Map<String, String> allowedSorts() { return ALLOWED_SORTS; }

    @Override
    protected String defaultSortColumn() { return DEFAULT_SORT; }
}
