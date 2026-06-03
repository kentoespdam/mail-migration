package id.perumdamts.mail.service.core.archive.numbering;

import id.perumdamts.mail.config.TenantConfig;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class LongRomanArchiveNumberGenerator extends AbstractArchiveNumberGenerator {

    private final TenantConfig tenantConfig;

    public LongRomanArchiveNumberGenerator(DSLContext dsl, TenantConfig tenantConfig) {
        super(dsl);
        this.tenantConfig = tenantConfig;
    }

    @Override
    public boolean supports(String pattern) {
        return "LONG_ROMAN".equalsIgnoreCase(pattern);
    }

    @Override
    protected String getFormatTemplate(String pattern) {
        String refCode = "ma_number_format_long_roman";
        return dsl.select(field("text"))
                .from(table("sys_reference"))
                .where(field("code").eq(refCode))
                .fetchOneInto(String.class);
    }

    @Override
    protected String getDefaultTemplate() {
        // e.g. 692.1/I/0406/2025
        return "#ma_cat#/#roman_month#/#seq#/#YYYY#";
    }

    @Override
    protected String getOfficeCode() {
        return tenantConfig.officeCode();
    }
}
