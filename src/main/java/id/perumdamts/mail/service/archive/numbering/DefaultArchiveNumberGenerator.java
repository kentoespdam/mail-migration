package id.perumdamts.mail.service.archive.numbering;

import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.domain.entity.MailArchive;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.jooq.impl.DSL.*;

@Component
public class DefaultArchiveNumberGenerator implements ArchiveNumberGenerator {

    private final DSLContext dsl;
    private final TenantConfig tenantConfig;

    public DefaultArchiveNumberGenerator(DSLContext dsl, TenantConfig tenantConfig) {
        this.dsl = dsl;
        this.tenantConfig = tenantConfig;
    }

    @Override
    public String generate(MailArchive archive) {
        String formatRef = tenantConfig.archiveNumberFormatRef();
        String template = getFormatTemplate(formatRef);
        if (template == null || template.isBlank()) {
            template = "#seq#/#org_code#/#ma_cat#/#YYYY#";
        }

        int sequence = getNextSequence(archive);
        LocalDate now = LocalDate.now();

        String categoryCode = "";
        if (archive.getCategory() != null) {
            categoryCode = archive.getCategory().getCode();
        }

        return template
                .replace("#seq#", String.valueOf(sequence))
                .replace("#org_code#", tenantConfig.officeCode())
                .replace("#ma_cat#", categoryCode)
                .replace("#YYYY#", now.format(DateTimeFormatter.ofPattern("yyyy")))
                .replace("#MM#", now.format(DateTimeFormatter.ofPattern("MM")));
    }

    private String getFormatTemplate(String refCode) {
        if (refCode == null || refCode.isBlank()) return null;
        return dsl.select(field("text"))
                .from(table("sys_reference"))
                .where(field("code").eq(refCode))
                .fetchOneInto(String.class);
    }

    private int getNextSequence(MailArchive archive) {
        Integer maxSeq = dsl.select(count())
                .from(table("mail_archive"))
                .where(field("ma_status").eq(2))
                .and(field("ma_category").eq(archive.getCategory() != null ? archive.getCategory().getId() : 0))
                .and(field("YEAR(ma_created_date)").eq(LocalDate.now().getYear()))
                .fetchOneInto(Integer.class);
        return (maxSeq != null ? maxSeq : 0) + 1;
    }
}
