package id.perumdamts.mail.integration.hr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Fallback saat HR Service tidak available.
 * Return empty/default — caller harus handle gracefully
 * (gunakan denormalisasi emp_name/pos_name dari mail_recipient sebagai fallback).
 */
@Component
public class HrServiceClientFallback implements HrServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HrServiceClientFallback.class);

    @Override
    public Optional<EmployeeDto> getEmployee(Long id) {
        log.warn("HR Service unavailable — getEmployee({})", id);
        return Optional.empty();
    }

    @Override
    public Optional<EmployeeDto> getByNipam(String nipam) {
        log.warn("HR Service unavailable — getByNipam({})", nipam);
        return Optional.empty();
    }

    @Override
    public Optional<EmployeeRingkasanDto> getRingkasan(Long id) {
        log.warn("HR Service unavailable — getRingkasan({})", id);
        return Optional.empty();
    }

    @Override
    public List<EmployeeDto> getBatchEmployees(BatchIdsRequest request) {
        log.warn("HR Service unavailable — getBatchEmployees({} ids)", request.ids().size());
        return List.of();
    }

    @Override
    public PageResponse<EmployeeDto> searchEmployees(String nama, String nipam, Long jabatanId,
                                                      Long organisasiId, String statusKerja,
                                                      int page, int size) {
        log.warn("HR Service unavailable — searchEmployees");
        return new PageResponse<>(List.of(), 0, 0, page, size);
    }

    @Override
    public List<JabatanDto> getJabatanList() {
        log.warn("HR Service unavailable — getJabatanList");
        return List.of();
    }

    @Override
    public List<OrganisasiDto> getOrganisasiList() {
        log.warn("HR Service unavailable — getOrganisasiList");
        return List.of();
    }
}
