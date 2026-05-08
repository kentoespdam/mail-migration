package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.enums.DispositionStatus;
import lombok.Value;

import java.time.LocalDate;

@Value
public class DispositionStatusResponse {
    DispositionStatus status;
    LocalDate deadline;
    Integer depth;
}
