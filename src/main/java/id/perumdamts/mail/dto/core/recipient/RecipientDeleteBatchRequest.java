package id.perumdamts.mail.dto.core.recipient;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecipientDeleteBatchRequest(
        @NotEmpty List<String> ids
) {}
