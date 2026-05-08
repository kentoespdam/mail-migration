package id.perumdamts.mail.security;

import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolver untuk mendapatkan activePosId (posisi/jabatan aktif) saat ini.
 *
 * <p>Priority resolution:
 * <ol>
 *   <li>Header X-Active-Position jika valid (milik user)</li>
 *   <li>JWT claim 'active_pos' jika ada</li>
 *   <li>Default posisi dari HR Service ({@code employeeDto.jabatanId()})</li>
 * </ol>
 *
 * <p>PLT detection: Jika posisi aktif berbeda dari posisi definitif,
 * maka user dalam mode PLT (Pelaksana Tugas).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailRoleContextResolver {

    private final HrServiceClient hrServiceClient;

    private static final String ACTIVE_POS_CLAIM = "active_pos";
    private static final String X_ACTIVE_POSITION = "X-Active-Position";

    private final Map<String, List<Long>> userPositionsCache = new ConcurrentHashMap<>();

    /**
     * Resolver activePosId dengan strategi fallback chain.
     *
     * @param userId         User ID (pegawaiId)
     * @param activePosClaim Nilai dari JWT claim 'active_pos' (bisa null)
     * @param headerPosition  Nilai dari header X-Active-Position (bisa null)
     * @return Resolved activePosId
     */
    public Long resolveActivePosition(String userId, Long activePosClaim, Long headerPosition) {
        Long defaultPosId = loadDefaultPosition(userId);

        // Priority 1: Header X-Active-Position
        if (headerPosition != null) {
            if (isPositionOwnedByUser(userId, headerPosition)) {
                log.debug("[ROLE] User {} menggunakan posisi header: {}", userId, headerPosition);
                return headerPosition;
            } else {
                log.warn("[ROLE] User {} mencoba posisi tidak owned: {}", userId, headerPosition);
                return null; // Will trigger 403 later
            }
        }

        // Priority 2: JWT claim
        if (activePosClaim != null) {
            log.debug("[ROLE] User {} menggunakan posisi dari JWT claim: {}", userId, activePosClaim);
            return activePosClaim;
        }

        // Priority 3: Default dari HR
        log.debug("[ROLE] User {} menggunakan default posisi: {}", userId, defaultPosId);
        return defaultPosId;
    }

    /**
     * Load default posisi dari HR Service (employee.jabatanId).
     */
    private Long loadDefaultPosition(String userId) {
        try {
            Long empId = Long.parseLong(userId);
            return hrServiceClient.getEmployee(empId)
                    .map(EmployeeDto::jabatanId)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("[ROLE] Gagal load posisi default untuk user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Cek apakah posisi tersebut milik user (di HR data).
     * Menggunakan cached list posisi user untuk efficiency.
     */
    private boolean isPositionOwnedByUser(String userId, Long positionId) {
        try {
            Long empId = Long.parseLong(userId);
            List<Long> userPositions = userPositionsCache.computeIfAbsent(userId, uid -> {
                return hrServiceClient.getEmployee(empId)
                        .map(emp -> List.of(emp.jabatanId()))
                        .orElse(List.of());
            });
            return userPositions.contains(positionId);
        } catch (Exception e) {
            log.warn("[ROLE] Gagal cek kepemilikan posisi: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cek apakah user dalam mode PLT (aktif ≠ definitif).
     */
    public boolean isPLT(String userId, Long activePosId) {
        if (activePosId == null) return false;
        try {
            Long empId = Long.parseLong(userId);
            Long definitifPosId = hrServiceClient.getEmployee(empId)
                    .map(EmployeeDto::jabatanId)
                    .orElse(null);
            return definitifPosId != null && !definitifPosId.equals(activePosId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract claim dari JWT payload map.
     */
    public static Long extractActivePosClaim(Map<String, Object> jwtPayload) {
        Object claim = jwtPayload.get(ACTIVE_POS_CLAIM);
        if (claim instanceof Number num) {
            return num.longValue();
        }
        return null;
    }
}