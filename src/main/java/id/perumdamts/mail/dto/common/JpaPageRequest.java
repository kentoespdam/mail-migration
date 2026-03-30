package id.perumdamts.mail.dto.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Getter
@Setter
public abstract class JpaPageRequest extends PageRequest {

    private String sortBy;
    private String sortDir;

    protected abstract Set<String> allowedSorts();

    protected abstract String defaultSort();

    public Sort toSort() {
        String col = (sortBy != null && allowedSorts().contains(sortBy)) ? sortBy : defaultSort();
        return Sort.by("desc".equalsIgnoreCase(sortDir) ? DESC : ASC, col);
    }

    public org.springframework.data.domain.PageRequest toPageable() {
        return org.springframework.data.domain.PageRequest.of(getPage(), getSize(), toSort());
    }
}
