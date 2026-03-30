package id.perumdamts.mail.dto.common;

import org.jooq.Field;
import org.jooq.SortField;

import java.util.Map;

import static org.jooq.impl.DSL.field;

public record SortParam(String sortBy, String sortDir) {

    /**
     * Resolve sort field using whitelist. Falls back to defaultColumn if sortBy is not in allowed map.
     */
    public static SortField<?> resolve(String sortBy, String sortDir,
                                        Map<String, String> allowedSorts, String defaultColumn) {
        String column = allowedSorts.getOrDefault(sortBy, defaultColumn);
        Field<Object> f = field(column);
        return "asc".equalsIgnoreCase(sortDir) ? f.asc() : f.desc();
    }
}
