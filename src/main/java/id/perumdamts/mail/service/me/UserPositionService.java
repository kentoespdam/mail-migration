package id.perumdamts.mail.service.me;

import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.dto.id.PositionId;
import id.perumdamts.mail.dto.me.PositionResponse;
import id.perumdamts.mail.security.MailPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPositionService {
    private final HrServiceClient hrClient;

    public List<PositionResponse> getPositionsForUser(MailPrincipal principal) {
        Long userId = Long.parseLong(principal.userId());
        EmployeeDto emp = hrClient.getEmployee(userId).orElse(null);
        if (emp == null) {
            return Collections.emptyList();
        }
        Long defPosId = emp.jabatanId();
        String defPosName = emp.jabatanNama();
        String unitName = emp.organisasiNama();
        boolean isActive = principal.activePosId() != null && principal.activePosId().equals(defPosId);
        boolean isPlt = principal.activePosId() != null && !principal.activePosId().equals(defPosId);
        PositionResponse defPos = new PositionResponse(new PositionId(defPosId), defPosName, unitName, isPlt, isActive);
        return List.of(defPos);
    }
}
