package id.perumdamts.mail.security;

import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailRoleContextResolverTest {

    @Mock
    private HrServiceClient hrServiceClient;

    private MailRoleContextResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MailRoleContextResolver(hrServiceClient);
    }

    @Test
    void resolveActivePosition_withHeaderOwnedByUser_returnsHeaderPosition() {
        when(hrServiceClient.getEmployee(1L)).thenReturn(Optional.of(
                new EmployeeDto(1L, "N001", "Test User", "AKTIF", null, null, null)
        ));

        Long result = resolver.resolveActivePosition("1", null, 100L);

        assertThat(result).isEqualTo(100L);
    }

    @Test
    void resolveActivePosition_withHeaderNotOwnedByUser_returnsNull() {
        when(hrServiceClient.getEmployee(1L)).thenReturn(Optional.of(
                new EmployeeDto(1L, "N001", "Test User", "AKTIF", null, null, null)
        ));

        Long result = resolver.resolveActivePosition("1", null, 999L);

        assertThat(result).isNull();
    }

    @Test
    void resolveActivePosition_withJwtClaimNoHeader_returnsJwtClaim() {
        when(hrServiceClient.getEmployee(1L)).thenReturn(Optional.of(
                new EmployeeDto(1L, "N001", "Test User", "AKTIF", null, null, null)
        ));

        Long result = resolver.resolveActivePosition("1", 200L, null);

        assertThat(result).isEqualTo(200L);
    }

    @Test
    void resolveActivePosition_noHeaderNoClaim_returnsDefaultFromHr() {
        when(hrServiceClient.getEmployee(1L)).thenReturn(Optional.of(
                new EmployeeDto(1L, "N001", "Test User", "AKTIF", 
                        new id.perumdamts.mail.integration.hr.JabatanDto(300L, "J01", "Staff", null),
                        null, null)
        ));

        Long result = resolver.resolveActivePosition("1", null, null);

        assertThat(result).isEqualTo(300L);
    }

    @Test
    void isPLT_withDifferentPosition_returnsTrue() {
        when(hrServiceClient.getEmployee(1L)).thenReturn(Optional.of(
                new EmployeeDto(1L, "N001", "Test User", "AKTIF",
                        new id.perumdamts.mail.integration.hr.JabatanDto(100L, "J01", "Sekretaris", null),
                        null, null)
        ));

        boolean result = resolver.isPLT("1", 200L);

        assertThat(result).isTrue();
    }

    @Test
    void isPLT_withSamePosition_returnsFalse() {
        when(hrServiceClient.getEmployee(1L)).thenReturn(Optional.of(
                new EmployeeDto(1L, "N001", "Test User", "AKTIF",
                        new id.perumdamts.mail.integration.hr.JabatanDto(100L, "J01", "Staff", null),
                        null, null)
        ));

        boolean result = resolver.isPLT("1", 100L);

        assertThat(result).isFalse();
    }

    @Test
    void isPLT_withNullActivePosId_returnsFalse() {
        boolean result = resolver.isPLT("1", null);

        assertThat(result).isFalse();
    }
}