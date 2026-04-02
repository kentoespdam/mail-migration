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

    private static final Set<String> ALLOWED = Set.of("name", "id", "status");

    private String search;

    @Override
    public Specification<DocumentType> toSpecification() {
        return (root, query, cb) -> search == null || search.isBlank()
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }

    @Override
    protected Set<String> allowedSorts() { return ALLOWED; }

    @Override
    protected String defaultSort() { return "name"; }
}
