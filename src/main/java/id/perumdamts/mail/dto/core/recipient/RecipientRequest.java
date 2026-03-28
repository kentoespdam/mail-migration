package id.perumdamts.mail.dto.core.recipient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecipientRequest(
        @NotNull @Positive Integer empId,
        @NotNull Integer circulation
) {}
