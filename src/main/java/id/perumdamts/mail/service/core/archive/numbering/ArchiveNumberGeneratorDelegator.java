package id.perumdamts.mail.service.core.archive.numbering;

import id.perumdamts.mail.entity.core.MailArchive;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class ArchiveNumberGeneratorDelegator implements ArchiveNumberGenerator {

    private final List<ArchiveNumberGenerator> generators;

    public ArchiveNumberGeneratorDelegator(List<ArchiveNumberGenerator> generators) {
        this.generators = generators;
    }

    @Override
    public String generate(MailArchive archive, String pattern) {
        ArchiveNumberGenerator generator = generators.stream()
                .filter(g -> g != this && g.supports(pattern))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No archive number generator found for pattern: " + pattern));
        return generator.generate(archive, pattern);
    }
    
    @Override
    public boolean supports(String pattern) {
        return true;
    }
}
