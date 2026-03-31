package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.JpaSearchRequest;
import id.perumdamts.mail.entity.master.DocumentType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@Getter
@Setter
public class DocumentTypeParams extends JpaSearchRequest<DocumentType> {

    private static final Set<String> ALLOWED = Set.of("name", "id");

    private String search;

    @Override
    public Specification<DocumentType> toSpecification() {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("status"), 1));
            if (search != null && !search.isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            return predicates;
        };
    }

    @Override
    protected Set<String> allowedSorts() { return ALLOWED; }

    @Override
    protected String defaultSort() { return "name"; }
}
