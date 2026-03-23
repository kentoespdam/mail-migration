package id.perumdamts.mail.service.mail.numbering;

import id.perumdamts.mail.domain.entity.Mail;

public interface MailNumberGenerator {
    String generate(Mail mail);
}
