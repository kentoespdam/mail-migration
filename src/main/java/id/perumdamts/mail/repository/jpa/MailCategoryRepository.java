package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.MailCategory;
import id.perumdamts.mail.domain.entity.MailType;
import id.perumdamts.mail.domain.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailCategoryRepository extends JpaRepository<MailCategory, Integer> {

    List<MailCategory> findByMailTypeIdOrderBySortAsc(Integer mailTypeId);

    boolean existsByMailTypeIdAndCode(Integer mailTypeId, String code);

    boolean existsByMailTypeId(Integer mailTypeId);

    long countByMailTypeAndStatusNot(MailType mailType, CategoryStatus status);
}
