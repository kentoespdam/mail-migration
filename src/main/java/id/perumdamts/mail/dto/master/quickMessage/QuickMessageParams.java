package id.perumdamts.mail.dto.master.quickMessage;

import id.perumdamts.mail.dto.common.PagedRequest;
import id.perumdamts.mail.enums.RecordStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class QuickMessageParams extends PagedRequest {

    private static final Map<String, String> ALLOWED = Map.of(
            "message", "ps.pesan",
            "status", "ps.status");

    private String search;
    private RecordStatus status;

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return "ps.pesan";
    }
}
