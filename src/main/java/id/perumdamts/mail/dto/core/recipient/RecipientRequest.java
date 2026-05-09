package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.id.CirculationTypeId;
import id.perumdamts.mail.dto.id.UserId;
import jakarta.validation.constraints.NotNull;

public record RecipientRequest(
        @NotNull UserId empId,
        @NotNull CirculationTypeId circulation) {
}
