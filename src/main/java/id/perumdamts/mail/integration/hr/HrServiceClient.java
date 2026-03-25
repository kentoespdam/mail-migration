package id.perumdamts.mail.integration.hr;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(
        name = "hr-service",
        url = "${hr.service.url}",
        fallback = HrServiceClientFallback.class,
        configuration = HrServiceConfig.class
)
public interface HrServiceClient {

    @GetMapping("/pegawai/{id}")
    @Cacheable(value = "hrEmployee", key = "'emp:' + #id")
    Optional<EmployeeDto> getEmployee(@PathVariable Long id);

    @GetMapping("/pegawai/{nipam}/nipam")
    Optional<EmployeeDto> getByNipam(@PathVariable String nipam);

    @GetMapping("/pegawai/{id}/ringkasan")
    Optional<EmployeeRingkasanDto> getRingkasan(@PathVariable Long id);

    @PostMapping("/pegawai/batch-by-ids")
    EmployeeResponse getBatchEmployees(@RequestBody BatchIdsRequest request);

    @GetMapping("/pegawai")
    PageResponse<EmployeeDto> searchEmployees(
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String nipam,
            @RequestParam(required = false) Long jabatanId,
            @RequestParam(required = false) Long organisasiId,
            @RequestParam(required = false) String statusKerja,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @GetMapping("/master/jabatan")
    List<JabatanDto> getJabatanList();

    @GetMapping("/master/organisasi/list")
    List<OrganisasiDto> getOrganisasiList();
}
