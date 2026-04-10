package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.PagedRequest;
import id.perumdamts.mail.enums.RecordStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DocumentTypeParams extends PagedRequest {

    private static final Map<String, String> ALLOWED = Map.of(
            "id", "jd.id",
            "name", "jd.jenis_dokumen",
            "status", "jd.status");

    private String search;
    private RecordStatus status;

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return "jd.jenis_dokumen";
    }
}
