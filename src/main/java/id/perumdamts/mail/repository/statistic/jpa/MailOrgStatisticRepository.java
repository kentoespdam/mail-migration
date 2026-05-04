package id.perumdamts.mail.repository.statistic.jpa;

import id.perumdamts.mail.entity.statistic.MailOrgStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailOrgStatisticRepository extends JpaRepository<MailOrgStatistic, Integer> {
    List<MailOrgStatistic> findByOrganizationId(Integer organizationId);
    List<MailOrgStatistic> findByPeriodMonth(Integer periodMonth);
}
