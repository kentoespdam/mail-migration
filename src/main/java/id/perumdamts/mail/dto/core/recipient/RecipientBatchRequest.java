package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.id.CirculationTypeId;
import id.perumdamts.mail.dto.id.UserId;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipientBatchRequest(
                @NotEmpty List<UserId> empIds,
                @NotNull CirculationTypeId circulation) {
}
