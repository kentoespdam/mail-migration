package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MailCategoryParams extends PagedRequest {

    private static final Map<String, String> ALLOWED = Map.of(
            "code", "mc.mcat_code",
            "name", "mc.mcat_name",
            "sort", "mc.sort"
    );

    private String search;
    private Long mailTypeId;

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return "mc.sort";
    }
}

