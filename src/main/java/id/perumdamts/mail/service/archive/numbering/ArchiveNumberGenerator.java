package id.perumdamts.mail.service.archive.numbering;

import id.perumdamts.mail.domain.entity.MailArchive;

public interface ArchiveNumberGenerator {
    String generate(MailArchive archive);
}
