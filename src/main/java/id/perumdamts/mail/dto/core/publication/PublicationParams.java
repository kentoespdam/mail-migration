package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.common.PagedRequest;
import id.perumdamts.mail.enums.PublicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class PublicationParams extends PagedRequest {

    private static final Map<String, String> ALLOWED = Map.of(
            "id","p.id",
            "publishedDate", "p.published_date",
            "title", "p.judul"
    );
    private static final String DEFAULT_SORT_COLUMN = "p.id";

    private PublicationStatus status;
    private String keyword;
    private Long typeId;
    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return DEFAULT_SORT_COLUMN;
    }
}
