package id.perumdamts.mail.service.core.mail.numbering;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/**
 * Mail number generator untuk tenant SMD (Surabaya Metro Daerah).
 * Format: #seq#/#org_code#/#m_cat#/#MR#/#YYYY#
 */
@Component
public class SmdMailNumberGenerator extends AbstractMailNumberGenerator {

    public SmdMailNumberGenerator(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public boolean supports(String clientCode) {
        return "SMD".equalsIgnoreCase(clientCode);
    }

    @Override
    protected String getOfficeCode() {
        return "SMD";
    }

    @Override
    protected String getFormatRefCode() {
        return "SMD_MAIL_NUMBER_FORMAT";
    }
}
