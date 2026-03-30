package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.dto.common.JpaSearchRequest;
import id.perumdamts.mail.entity.master.MailCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@Getter
@Setter
public class MailCategoryParams extends JpaSearchRequest<MailCategory> {

    private static final Set<String> ALLOWED = Set.of("code", "name", "sort");

    private String search;
    private Integer mailTypeId;

    @Override
    public Specification<MailCategory> toSpecification() {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (mailTypeId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("mailType").get("id"), mailTypeId));
            }
            if (search != null && !search.isBlank()) {
                String kw = "%" + search.toLowerCase() + "%";
                predicates = cb.and(predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("name")), kw),
                                cb.like(cb.lower(root.get("code")), kw)
                        ));
            }
            return predicates;
        };
    }

    @Override
    protected Set<String> allowedSorts() { return ALLOWED; }

    @Override
    protected String defaultSort() { return "sort"; }
}
