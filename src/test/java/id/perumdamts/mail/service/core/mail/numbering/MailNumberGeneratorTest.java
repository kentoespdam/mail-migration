package id.perumdamts.mail.service.core.mail.numbering;

import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailNumberGeneratorTest {

    @Mock
    private DSLContext dsl;

    private BmsMailNumberGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new BmsMailNumberGenerator(dsl) {
            @Override
            protected String getTemplate() {
                // Using the template from legacy data bukti
                return "#seq#/#org_code#/#m_cat#-#type#/#MR#/#YYYY#";
            }

            @Override
            protected int getNextSequence(Mail mail, int year) {
                return 22460;
            }

            @Override
            protected String getOfficeCode() {
                return "00";
            }
        };
    }

    @Test
    void testGenerate_Internal_October() {
        Mail mail = new Mail();
        mail.setCreatedDate(LocalDateTime.of(2025, 10, 15, 10, 0));
        
        MailType mailType = new MailType();
        mailType.setName("Internal");
        mail.setMailType(mailType);
        
        MailCategory mailCategory = new MailCategory();
        mailCategory.setCode("485.1");
        mail.setMailCategory(mailCategory);

        String result = generator.generate(mail);

        // Expected format matching legacy: 22460/00/485.1-I/X/2025
        assertEquals("22460/00/485.1-I/X/2025", result);
    }

    @Test
    void testGenerate_Masuk_July() {
        generator = new BmsMailNumberGenerator(dsl) {
            @Override
            protected String getTemplate() {
                return "#seq#/#org_code#/#m_cat#-#type#/#MR#/#YYYY#";
            }

            @Override
            protected int getNextSequence(Mail mail, int year) {
                return 49;
            }
            
            @Override
            protected String getOfficeCode() {
                return "1421002";
            }
        };

        Mail mail = new Mail();
        mail.setCreatedDate(LocalDateTime.of(2025, 7, 20, 10, 0));

        MailType mailType = new MailType();
        mailType.setName("Masuk");
        mail.setMailType(mailType);

        MailCategory mailCategory = new MailCategory();
        mailCategory.setCode("SEKRE-M");
        mail.setMailCategory(mailCategory);

        String result = generator.generate(mail);

        // Expected: 49/1421002/SEKRE-M-M/VII/2025
        assertEquals("49/1421002/SEKRE-M-M/VII/2025", result);
    }

    @Test
    void testGenerate_Keluar_January() {
        generator = new BmsMailNumberGenerator(dsl) {
            @Override
            protected String getTemplate() {
                return "#seq#/#org_code#/#m_cat#-#type#/#MR#/#YYYY#";
            }

            @Override
            protected int getNextSequence(Mail mail, int year) {
                return 100;
            }
            
            @Override
            protected String getOfficeCode() {
                return "BMS";
            }
        };

        Mail mail = new Mail();
        mail.setCreatedDate(LocalDateTime.of(2026, 1, 5, 10, 0));

        MailType mailType = new MailType();
        mailType.setName("Keluar");
        mail.setMailType(mailType);

        MailCategory mailCategory = new MailCategory();
        mailCategory.setCode("DIR");
        mail.setMailCategory(mailCategory);

        String result = generator.generate(mail);

        // Expected: 100/BMS/DIR-K/I/2026
        assertEquals("100/BMS/DIR-K/I/2026", result);
    }
    
    @Test
    void testGenerate_NullTypeOrCategory() {
        generator = new BmsMailNumberGenerator(dsl) {
            @Override
            protected String getTemplate() {
                return "#seq#/#org_code#/#m_cat#-#type#/#MR#/#YYYY#";
            }

            @Override
            protected int getNextSequence(Mail mail, int year) {
                return 1;
            }
        };

        Mail mail = new Mail();
        mail.setCreatedDate(LocalDateTime.of(2026, 2, 1, 0, 0));
        // mailType and mailCategory are null

        String result = generator.generate(mail);

        // Expected: 1/BMS/-/II/2026 (since categoryCode is "" and typeCode is "")
        assertEquals("1/BMS/-/II/2026", result);
    }
}
