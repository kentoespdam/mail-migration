package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MailTypeParams extends PagedRequest {

    private static final Map<String, String> ALLOWED = Map.of(
            "name", "mt.mail_type",
            "status", "mt.mail_type_status"
    );

    private String search;

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSortColumn() {
        return "mt.mail_type";
    }
}

