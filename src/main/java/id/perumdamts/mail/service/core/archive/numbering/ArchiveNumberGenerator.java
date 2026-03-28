package id.perumdamts.mail.service.core.archive.numbering;

import id.perumdamts.mail.entity.core.MailArchive;

public interface ArchiveNumberGenerator {
    String generate(MailArchive archive);
}
