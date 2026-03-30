package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MailCategoryRepository extends JpaRepository<MailCategory, Integer>, JpaSpecificationExecutor<MailCategory> {

    List<MailCategory> findByMailTypeIdOrderBySortAsc(Integer mailTypeId);

    boolean existsByMailTypeIdAndCode(Integer mailTypeId, String code);

    boolean existsByMailTypeId(Integer mailTypeId);

    long countByMailTypeAndStatusNot(MailType mailType, CategoryStatus status);
}
