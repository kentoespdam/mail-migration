package id.perumdamts.mail.api.dto.recipient;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipientBatchRequest(
        @NotEmpty List<Integer> empIds,
        @NotNull Integer circulation
) {}
