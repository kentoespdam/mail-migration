package id.perumdamts.mail.service.core.mail.numbering;

import id.perumdamts.mail.config.TenantConfig;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/**
 * Fallback mail number generator jika tenant-specific generator tidak tersedia.
 * Menggunakan format default: #seq#/#org_code#/#m_cat#/#MR#/#YYYY#
 */
@Component
public class DefaultMailNumberGenerator extends AbstractMailNumberGenerator {

    private final TenantConfig tenantConfig;

    public DefaultMailNumberGenerator(DSLContext dsl, TenantConfig tenantConfig) {
        super(dsl);
        this.tenantConfig = tenantConfig;
    }

    @Override
    public boolean supports(String clientCode) {
        // Support semua client code sebagai fallback
        return true;
    }

    @Override
    protected String getOfficeCode() {
        return tenantConfig.officeCode();
    }

    @Override
    protected String getFormatRefCode() {
        return tenantConfig.mailNumberFormatRef();
    }
}
