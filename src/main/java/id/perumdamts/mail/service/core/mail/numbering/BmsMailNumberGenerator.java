package id.perumdamts.mail.service.core.mail.numbering;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/**
 * Mail number generator untuk tenant BMS (Bandung Metro Selatan).
 * Format: #seq#/#org_code#/#m_cat#/#MR#/#YYYY#
 */
@Component
public class BmsMailNumberGenerator extends AbstractMailNumberGenerator {

    public BmsMailNumberGenerator(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public boolean supports(String clientCode) {
        return "BMS".equalsIgnoreCase(clientCode);
    }

    @Override
    protected String getOfficeCode() {
        return "BMS";
    }

    @Override
    protected String getFormatRefCode() {
        return "BMS_MAIL_NUMBER_FORMAT";
    }
}
