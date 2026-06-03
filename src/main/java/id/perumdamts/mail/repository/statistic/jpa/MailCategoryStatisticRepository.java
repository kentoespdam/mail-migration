package id.perumdamts.mail.repository.statistic.jpa;

import id.perumdamts.mail.entity.statistic.MailCategoryStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailCategoryStatisticRepository extends JpaRepository<MailCategoryStatistic, Long> {
    Optional<MailCategoryStatistic> findByPeriodMonthAndCategoryId(Integer periodMonth, Long categoryId);
}
