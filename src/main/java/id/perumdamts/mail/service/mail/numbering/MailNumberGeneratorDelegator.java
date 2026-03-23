package id.perumdamts.mail.service.mail.numbering;

import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.domain.entity.Mail;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Delegating MailNumberGenerator yang memilih generator berdasarkan tenant.
 * Fix bug di source PHP: blok SMD tidak pakai else if, sehingga di-override oleh BPN.
 */
@Component
@Primary
@Order(1)
public class MailNumberGeneratorDelegator implements MailNumberGenerator {

    private final List<MailNumberGenerator> generators;
    private final TenantConfig tenantConfig;

    public MailNumberGeneratorDelegator(List<MailNumberGenerator> generators, TenantConfig tenantConfig) {
        this.generators = generators;
        this.tenantConfig = tenantConfig;
    }

    @Override
    public String generate(Mail mail) {
        String clientCode = tenantConfig.code();
        MailNumberGenerator generator = generators.stream()
                .filter(g -> g.supports(clientCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No mail number generator found for client code: " + clientCode));
        return generator.generate(mail);
    }

    @Override
    public boolean supports(String clientCode) {
        return generators.stream().anyMatch(g -> g.supports(clientCode));
    }
}
