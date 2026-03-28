package id.perumdamts.mail.service.core.mail.numbering;

import id.perumdamts.mail.entity.core.Mail;

/**
 * Strategy interface untuk generate nomor surat per tenant.
 * Setiap tenant (BMS/SMD/BPN) bisa punya format nomor surat berbeda.
 */
public interface MailNumberGenerator {
    
    /**
     * Generate nomor surat untuk mail yang diberikan.
     * @param mail mail yang akan di-generate nomor suratnya
     * @return nomor surat yang di-generate
     */
    String generate(Mail mail);
    
    /**
     * Check apakah generator ini support tenant tertentu.
     * @param clientCode client code tenant
     * @return true jika support
     */
    boolean supports(String clientCode);
}
