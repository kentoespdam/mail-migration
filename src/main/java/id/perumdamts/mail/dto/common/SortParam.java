package id.perumdamts.mail.dto.common;

import org.jooq.Field;
import org.jooq.SortField;

import java.util.Map;

import static org.jooq.impl.DSL.field;

public record SortParam(String sortBy, String sortDir) {

    /**
     * Resolve sort field using whitelist. Falls back to defaultColumn if sortBy is null or not in allowed map.
     */
    public static SortField<?> resolve(String sortBy, String sortDir,
                                        Map<String, String> allowedSorts, String defaultColumn) {
        // Handle null or blank sortBy - use default column
        String column = (sortBy == null || sortBy.isBlank()) 
                ? defaultColumn 
                : allowedSorts.getOrDefault(sortBy, defaultColumn);
        
        Field<Object> f = field(column);
        return "asc".equalsIgnoreCase(sortDir) ? f.asc() : f.desc();
    }
}
