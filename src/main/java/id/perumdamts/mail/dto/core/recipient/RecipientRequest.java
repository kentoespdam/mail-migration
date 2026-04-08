package id.perumdamts.mail.dto.core.recipient;

import jakarta.validation.constraints.NotNull;

public record RecipientRequest(
        @NotNull String empId,
        @NotNull String circulation) {
}
