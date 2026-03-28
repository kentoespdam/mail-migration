package id.perumdamts.mail.api.dto.recipient;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecipientDeleteBatchRequest(
        @NotEmpty List<Long> ids
) {}
