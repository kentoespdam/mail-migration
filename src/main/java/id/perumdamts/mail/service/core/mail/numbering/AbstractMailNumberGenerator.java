package id.perumdamts.mail.service.core.mail.numbering;

import id.perumdamts.mail.entity.core.Mail;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        LocalDateTime referenceDate = mail.getCreatedDate() != null ? mail.getCreatedDate() : LocalDateTime.now();
        int sequence = getNextSequence(mail, referenceDate.getYear());

        String categoryCode = mail.getMailCategory() != null ? mail.getMailCategory().getCode() : "";
        String typeCode = (mail.getMailType() != null && mail.getMailType().getName() != null && !mail.getMailType().getName().isBlank())
                ? mail.getMailType().getName().substring(0, 1).toUpperCase()
                : "";
        String romanMonth = getRomanMonth(referenceDate.getMonthValue());

        return template
                .replace("#seq#", String.valueOf(sequence))
                .replace("#org_code#", getOfficeCode())
                .replace("#m_cat#", categoryCode)
                .replace("#type#", typeCode)
                .replace("#MR#", romanMonth)
                .replace("#YYYY#", referenceDate.format(DateTimeFormatter.ofPattern("yyyy")))
                .replace("#MM#", referenceDate.format(DateTimeFormatter.ofPattern("MM")));
    }

    private String getRomanMonth(int month) {
        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
        return (month >= 1 && month <= 12) ? romans[month] : "";
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
        return dsl.select(field("text", String.class))
                .from(table("sys_reference"))
                .where(field("code").eq(refCode))
                .fetchOneInto(String.class);
    }

    /**
     * Get next sequence number dengan SELECT FOR UPDATE untuk race condition safety.
     * Sequence di-reset setiap tahun.
     */
    protected int getNextSequence(Mail mail, int year) {
        int categoryId = mail.getMailCategory() != null ? mail.getMailCategory().getId().intValue() : 0;

        // Lock row untuk prevent race condition dan gunakan MAX dari parsed seq
        Integer maxSeq = dsl.select(max(cast(substringIndex(field("m_no", String.class), "/", 1), Integer.class)))
                .from(table("mail"))
                .where(field("m_category").eq(categoryId))
                .and(field("YEAR(m_created_date)").eq(year))
                .and(field("m_no").isNotNull())
                .and(field("m_no").notEqual(""))
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
