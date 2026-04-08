package id.perumdamts.mail.dto.core.recipient;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipientBatchRequest(
                @NotEmpty List<String> empIds,
                @NotNull String circulation) {
}
