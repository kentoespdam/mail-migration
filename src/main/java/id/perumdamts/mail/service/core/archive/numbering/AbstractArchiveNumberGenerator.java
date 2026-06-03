package id.perumdamts.mail.service.core.archive.numbering;

import id.perumdamts.mail.entity.core.MailArchive;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.jooq.impl.DSL.*;

@Transactional
public abstract class AbstractArchiveNumberGenerator implements ArchiveNumberGenerator {

    protected final DSLContext dsl;

    protected AbstractArchiveNumberGenerator(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String generate(MailArchive archive, String pattern) {
        String template = getFormatTemplate(pattern);
        if (template == null || template.isBlank()) {
            template = getDefaultTemplate();
        }

        int sequence = getNextSequence(pattern);
        LocalDate now = LocalDate.now();

        String categoryCode = "";
        if (archive.getCategory() != null) {
            categoryCode = archive.getCategory().getCode();
        }

        return template
                .replace("#seq#", String.format("%04d", sequence))
                .replace("#org_code#", getOfficeCode())
                .replace("#ma_cat#", categoryCode)
                .replace("#roman_month#", toRoman(now.getMonthValue()))
                .replace("#YYYY#", now.format(DateTimeFormatter.ofPattern("yyyy")))
                .replace("#MM#", now.format(DateTimeFormatter.ofPattern("MM")));
    }

    protected int getNextSequence(String patternCode) {
        int year = LocalDate.now().getYear();

        // Use INSERT ... ON DUPLICATE KEY UPDATE or SELECT FOR UPDATE to get next seq
        // With jooq we can do:
        dsl.insertInto(table("mail_archive_seq"))
           .columns(field("year"), field("pattern_code"), field("last_seq"))
           .values(year, patternCode, 1)
           .onDuplicateKeyUpdate()
           .set(field("last_seq", Integer.class), field("last_seq", Integer.class).plus(1))
           .execute();

        return dsl.select(field("last_seq", Integer.class))
                  .from(table("mail_archive_seq"))
                  .where(field("year").eq(year))
                  .and(field("pattern_code").eq(patternCode))
                  .fetchOneInto(Integer.class);
    }
    
    private String toRoman(int month) {
        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
        if (month >= 1 && month <= 12) {
            return romans[month];
        }
        return "";
    }

    protected abstract String getFormatTemplate(String pattern);

    protected abstract String getDefaultTemplate();

    protected abstract String getOfficeCode();
}
