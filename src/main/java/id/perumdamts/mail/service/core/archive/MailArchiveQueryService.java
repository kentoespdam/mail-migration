package id.perumdamts.mail.service.core.archive;

import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.archive.ArchiveReportRequest;
import id.perumdamts.mail.dto.core.archive.ArchiveReportResponse;
import id.perumdamts.mail.dto.core.archive.ArchiveSearchRequest;
import id.perumdamts.mail.dto.core.archive.ArchiveSummaryResponse;
import id.perumdamts.mail.repository.core.jooq.ArchiveQueryRepository;
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

    public PagedResponse<ArchiveSummaryResponse> findForAdmin(ArchiveSearchRequest request) {
        List<ArchiveSummaryResponse> items = archiveQueryRepository.findForAdmin(request, tenantConfig.officeCode());
        long total = items.isEmpty() ? 0 : items.getFirst().totalCount();
        return PagedResponse.of(items, request, total);
    }

    public PagedResponse<ArchiveSummaryResponse> searchWithAcl(ArchiveSearchRequest request,
            List<Long> positionIds) {
        List<ArchiveSummaryResponse> items = archiveQueryRepository.searchWithAcl(request, positionIds,
                tenantConfig.officeCode());
        long total = items.isEmpty() ? 0 : items.getFirst().totalCount();
        return PagedResponse.of(items, request, total);
    }

    public PagedResponse<ArchiveReportResponse> getReport(ArchiveReportRequest request) {
        List<ArchiveReportResponse> items = archiveQueryRepository.getReport(request, tenantConfig.officeCode());
        long total = items.isEmpty() ? 0 : items.getFirst().totalCount();
        return PagedResponse.of(items, request, total);
    }
}
