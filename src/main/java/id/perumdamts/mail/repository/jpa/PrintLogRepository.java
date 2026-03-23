package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.PrintLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrintLogRepository extends JpaRepository<PrintLog, Long> {
    
    /**
     * Find print log by auth code untuk verifikasi.
     */
    Optional<PrintLog> findByAuthCode(String authCode);
    
    /**
     * Find all print logs untuk mail tertentu.
     */
    List<PrintLog> findByMailIdOrderByPrintDateDesc(Integer mailId);
}
