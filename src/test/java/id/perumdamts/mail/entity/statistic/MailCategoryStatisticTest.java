package id.perumdamts.mail.entity.statistic;

import id.perumdamts.mail.entity.master.MailCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class MailCategoryStatisticTest {

    @Test
    void testEntity() {
        MailCategory category = mock(MailCategory.class);
        MailCategoryStatistic mcs = new MailCategoryStatistic();
        mcs.setId(1L);
        mcs.setPeriodMonth(202310);
        mcs.setCategory(category);
        mcs.setTotal(100);

        assertEquals(1L, mcs.getId());
        assertEquals(202310, mcs.getPeriodMonth());
        assertEquals(category, mcs.getCategory());
        assertEquals(100, mcs.getTotal());

        mcs.onUpdate();
        assertNotNull(mcs.getUpdatedAt());
    }
}
