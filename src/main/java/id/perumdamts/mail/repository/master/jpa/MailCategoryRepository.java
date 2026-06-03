package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MailCategoryRepository extends JpaRepository<MailCategory, Long>, JpaSpecificationExecutor<MailCategory> {
    boolean existsByMailTypeIdAndCode(Long mailTypeId, String code);

    long countByMailTypeAndStatusNot(MailType mailType, CategoryStatus status);

    java.util.List<MailCategory> findAllByStatusOrderByIdAsc(CategoryStatus status);
}
