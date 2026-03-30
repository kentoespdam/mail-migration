package id.perumdamts.mail.dto.core.archive;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class ArchiveReportRequest extends PagedRequest {

    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "categoryName",  "mc.mcat_name",
            "year",          "a.ma_year",
            "totalArchives", "totalArchives"
    );
    private static final String DEFAULT_SORT = "a.ma_year";

    private Integer categoryId;
    private Short year;
    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    protected Map<String, String> allowedSorts() { return ALLOWED_SORTS; }

    @Override
    protected String defaultSortColumn() { return DEFAULT_SORT; }
}
