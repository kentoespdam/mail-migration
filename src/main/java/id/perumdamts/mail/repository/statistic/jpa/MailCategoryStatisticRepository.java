package id.perumdamts.mail.repository.statistic.jpa;

import id.perumdamts.mail.entity.statistic.MailCategoryStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailCategoryStatisticRepository extends JpaRepository<MailCategoryStatistic, Integer> {
    List<MailCategoryStatistic> findByPeriodMonth(Integer periodMonth);
    List<MailCategoryStatistic> findByCategoryId(Long categoryId);
}
