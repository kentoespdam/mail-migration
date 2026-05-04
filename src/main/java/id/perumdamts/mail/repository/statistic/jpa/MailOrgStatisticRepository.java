package id.perumdamts.mail.repository.statistic.jpa;

import id.perumdamts.mail.entity.statistic.MailOrgStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailOrgStatisticRepository extends JpaRepository<MailOrgStatistic, Long> {
    List<MailOrgStatistic> findByCreatedByOrg(Integer createdByOrg);
    List<MailOrgStatistic> findByPeriodMonth(Integer periodMonth);
}
