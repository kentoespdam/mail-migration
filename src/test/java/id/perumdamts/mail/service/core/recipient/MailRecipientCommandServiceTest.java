package id.perumdamts.mail.service.core.recipient;

import id.perumdamts.mail.dto.core.recipient.RecipientRequest;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.enums.CirculationType;
import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.integration.hr.JabatanDto;
import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailRecipientCommandServiceTest {

    @Mock
    private MailRecipientRepository recipientRepository;

    @Mock
    private MailRepository mailRepository;

    @Mock
    private HrServiceClient hrServiceClient;

    @Mock
    private SqidsEncoder encoder;

    @InjectMocks
    private MailRecipientCommandService commandService;

    @Test
    void addRecipient_whenAlreadyExists_throwsException() {
        Long mailId = 1L;
        Long userId = 100L;
        Long empId = 1000L;
        RecipientRequest request = new RecipientRequest("sqid-emp", "sqid-circ");
        
        Mail mail = new Mail();
        mail.setId(mailId);
        mail.setCreatedBy(userId); 
        
        JabatanDto jabatan = new JabatanDto(10L, "J01", "Manager", null);
        EmployeeDto emp = new EmployeeDto(userId, "EMP001", "John Doe", "Active", jabatan, null, null);
        
        when(mailRepository.findById(mailId)).thenReturn(Optional.of(mail));
        when(encoder.decode(CirculationType.class, "sqid-circ")).thenReturn(1L);
        when(encoder.decode(MailRecipient.class, "sqid-emp")).thenReturn(empId);
        when(hrServiceClient.getEmployee(empId)).thenReturn(Optional.of(emp));
        when(recipientRepository.existsByMailIdAndUserId(mailId, userId)).thenReturn(true);
        
        assertThatThrownBy(() -> commandService.addRecipient(mailId, request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recipient already exists for this mail");
    }
}
