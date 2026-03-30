package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.dto.common.JpaSearchRequest;
import id.perumdamts.mail.entity.master.AllowedFileType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@Getter
@Setter
public class AllowedFileTypeParams extends JpaSearchRequest<AllowedFileType> {
    private static final Set<String> ALLOWED = Set.of("context", "extension", "id");

    private String search;
    private String context;

    @Override
    public Specification<AllowedFileType> toSpecification() {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (context != null && !context.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("context"), context.toUpperCase()));
                predicates = cb.and(predicates,
                        cb.equal(root.get("isActive"), true));
            }
            if (search != null && !search.isBlank()) {
                String kw = "%" + search.toLowerCase() + "%";
                predicates = cb.and(predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("context")), kw),
                                cb.like(cb.lower(root.get("extension")), kw)
                        ));
            }
            return predicates;
        };
    }

    @Override
    protected Set<String> allowedSorts() {
        return ALLOWED;
    }

    @Override
    protected String defaultSort() {
        return "context";
    }
}
