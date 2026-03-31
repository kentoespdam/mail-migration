package id.perumdamts.mail.dto.master.quickMessage;

import id.perumdamts.mail.dto.common.JpaPageRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class QuickMessageParams extends JpaPageRequest {

    private static final Set<String> ALLOWED = Set.of("message", "status", "createdDate");

    private String search;

    @Override
    protected Set<String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSort() {
        return "message";
    }
}
