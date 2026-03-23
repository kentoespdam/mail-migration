package id.perumdamts.mail.service.archive;

import id.perumdamts.mail.api.dto.archive.ArchiveReportRequest;
import id.perumdamts.mail.api.dto.archive.ArchiveReportResponse;
import id.perumdamts.mail.api.dto.archive.ArchiveSearchRequest;
import id.perumdamts.mail.api.dto.archive.ArchiveSummaryResponse;
import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.repository.jooq.ArchiveQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailArchiveQueryService {

    private final ArchiveQueryRepository archiveQueryRepository;
    private final TenantConfig tenantConfig;

    public MailArchiveQueryService(ArchiveQueryRepository archiveQueryRepository,
                                    TenantConfig tenantConfig) {
        this.archiveQueryRepository = archiveQueryRepository;
        this.tenantConfig = tenantConfig;
    }

    public List<ArchiveSummaryResponse> findForAdmin(ArchiveSearchRequest request) {
        return archiveQueryRepository.findForAdmin(request, tenantConfig.officeCode());
    }

    public List<ArchiveSummaryResponse> searchWithAcl(ArchiveSearchRequest request,
                                                       List<Integer> positionIds) {
        return archiveQueryRepository.searchWithAcl(request, positionIds, tenantConfig.officeCode());
    }

    public List<ArchiveReportResponse> getReport(ArchiveReportRequest request) {
        return archiveQueryRepository.getReport(request, tenantConfig.officeCode());
    }
}
