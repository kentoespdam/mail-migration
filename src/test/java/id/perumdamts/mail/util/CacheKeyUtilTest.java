package id.perumdamts.mail.util;

import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.enums.PublicationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyUtilTest {

    @Test
    void publicationKey_shouldIncludePagingAndFilters() {
        PublicationParams params = new PublicationParams();
        params.setPage(2);
        params.setSize(25);
        params.setSortBy("publishedDate");
        params.setSortDir("desc");
        params.setStatus(PublicationStatus.PUBLISHED);
        params.setKeyword("annual report");
        params.setTypeId("type-7");
        params.setStartDate(LocalDate.of(2026, 1, 1));
        params.setEndDate(LocalDate.of(2026, 1, 31));

        assertThat(CacheKeyUtil.publicationKey(params))
                .isEqualTo("publication:2:25:publishedDate:desc:PUBLISHED:annual report:type-7:2026-01-01:2026-01-31");
    }
}
