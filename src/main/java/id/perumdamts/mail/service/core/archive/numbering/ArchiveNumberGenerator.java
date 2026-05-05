package id.perumdamts.mail.service.core.archive.numbering;

import id.perumdamts.mail.entity.core.MailArchive;

public interface ArchiveNumberGenerator {
    String generate(MailArchive archive, String pattern);
    
    /**
     * Checks if this generator supports the given pattern code.
     */
    default boolean supports(String pattern) {
        return false;
    }
}
