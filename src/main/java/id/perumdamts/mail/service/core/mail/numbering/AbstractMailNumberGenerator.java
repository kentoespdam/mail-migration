package id.perumdamts.mail.service.core.mail.numbering;

import id.perumdamts.mail.entity.core.Mail;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.jooq.impl.DSL.*;

/**
 * Base implementation untuk MailNumberGenerator.
 * Menyediakan logic umum untuk load template dari sys_reference dan generate sequence.
 */
@Transactional
public abstract class AbstractMailNumberGenerator implements MailNumberGenerator {

    protected final DSLContext dsl;

    protected AbstractMailNumberGenerator(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String generate(Mail mail) {
        String template = getTemplate();
        if (template == null || template.isBlank()) {
            template = getDefaultTemplate();
        }

        int sequence = getNextSequence(mail);
        LocalDate now = LocalDate.now();

        String categoryCode = mail.getMailCategory() != null ? mail.getMailCategory().getCode() : "";

        return template
                .replace("#seq#", String.valueOf(sequence))
                .replace("#org_code#", getOfficeCode())
                .replace("#m_cat#", categoryCode)
                .replace("#MR#", "MR")
                .replace("#YYYY#", now.format(DateTimeFormatter.ofPattern("yyyy")))
                .replace("#MM#", now.format(DateTimeFormatter.ofPattern("MM")));
    }

    /**
     * Get template dari sys_reference table.
     * Override di subclass untuk specify code yang berbeda per tenant.
     */
    protected String getTemplate() {
        String refCode = getFormatRefCode();
        if (refCode == null || refCode.isBlank()) {
            return null;
        }
        return dsl.select(field("text"))
                .from(table("sys_reference"))
                .where(field("code").eq(refCode))
                .fetchOneInto(String.class);
    }

    /**
     * Get next sequence number dengan SELECT FOR UPDATE untuk race condition safety.
     * Sequence di-reset setiap tahun.
     */
    protected int getNextSequence(Mail mail) {
        int categoryId = mail.getMailCategory() != null ? mail.getMailCategory().getId().intValue() : 0;
        int year = LocalDate.now().getYear();

        // Lock row untuk prevent race condition
        Integer maxSeq = dsl.select(count())
                .from(table("mail"))
                .where(field("m_status").eq(1))
                .and(field("m_category").eq(categoryId))
                .and(field("YEAR(m_created_date)").eq(year))
                .forUpdate()
                .fetchOneInto(Integer.class);

        return (maxSeq != null ? maxSeq : 0) + 1;
    }

    /**
     * Get client code untuk tenant ini.
     * Override di subclass.
     */
    protected abstract String getOfficeCode();

    /**
     * Get format ref code untuk load template dari sys_reference.
     * Override di subclass.
     */
    protected abstract String getFormatRefCode();

    /**
     * Default template jika tidak ada di sys_reference.
     */
    protected String getDefaultTemplate() {
        return "#seq#/#org_code#/#m_cat#/#MR#/#YYYY#";
    }
}
