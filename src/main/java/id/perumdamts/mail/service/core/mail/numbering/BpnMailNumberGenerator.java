package id.perumdamts.mail.service.core.mail.numbering;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/**
 * Mail number generator untuk tenant BPN (Badan Pertanahan Nasional).
 * Format: #seq#/#org_code#/#m_cat#/#MR#/#YYYY#
 */
@Component
public class BpnMailNumberGenerator extends AbstractMailNumberGenerator {

    public BpnMailNumberGenerator(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public boolean supports(String clientCode) {
        return "BPN".equalsIgnoreCase(clientCode);
    }

    @Override
    protected String getOfficeCode() {
        return "BPN";
    }

    @Override
    protected String getFormatRefCode() {
        return "BPN_MAIL_NUMBER_FORMAT";
    }
}
