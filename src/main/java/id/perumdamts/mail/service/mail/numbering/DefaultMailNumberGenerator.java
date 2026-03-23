package id.perumdamts.mail.service.mail.numbering;

import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.domain.entity.Mail;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.jooq.impl.DSL.*;

@Component
public class DefaultMailNumberGenerator implements MailNumberGenerator {

    private final DSLContext dsl;
    private final TenantConfig tenantConfig;

    public DefaultMailNumberGenerator(DSLContext dsl, TenantConfig tenantConfig) {
        this.dsl = dsl;
        this.tenantConfig = tenantConfig;
    }

    @Override
    public String generate(Mail mail) {
        String formatRef = tenantConfig.mailNumberFormatRef();
        String template = getFormatTemplate(formatRef);
        if (template == null || template.isBlank()) {
            template = "#seq#/#org_code#/#m_cat#/#MR#/#YYYY#";
        }

        int sequence = getNextSequence(mail);
        LocalDate now = LocalDate.now();

        String categoryCode = "";
        if (mail.getMailCategory() != null) {
            categoryCode = mail.getMailCategory().getCode();
        }

        String result = template
                .replace("#seq#", String.valueOf(sequence))
                .replace("#org_code#", tenantConfig.officeCode())
                .replace("#m_cat#", categoryCode)
                .replace("#MR#", "MR")
                .replace("#YYYY#", now.format(DateTimeFormatter.ofPattern("yyyy")))
                .replace("#MM#", now.format(DateTimeFormatter.ofPattern("MM")));

        return result;
    }

    private String getFormatTemplate(String refCode) {
        if (refCode == null || refCode.isBlank()) return null;
        return dsl.select(field("text"))
                .from(table("sys_reference"))
                .where(field("code").eq(refCode))
                .fetchOneInto(String.class);
    }

    private int getNextSequence(Mail mail) {
        Integer maxSeq = dsl.select(count())
                .from(table("mail"))
                .where(field("m_status").eq(1))
                .and(field("m_category").eq(mail.getMailCategory() != null ? mail.getMailCategory().getId() : 0))
                .and(field("YEAR(m_created_date)").eq(LocalDate.now().getYear()))
                .fetchOneInto(Integer.class);
        return (maxSeq != null ? maxSeq : 0) + 1;
    }
}
