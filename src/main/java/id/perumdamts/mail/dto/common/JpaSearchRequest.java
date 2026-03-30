package id.perumdamts.mail.dto.common;

import org.springframework.data.jpa.domain.Specification;

public abstract class JpaSearchRequest<T> extends JpaPageRequest {

    public abstract Specification<T> toSpecification();
}
