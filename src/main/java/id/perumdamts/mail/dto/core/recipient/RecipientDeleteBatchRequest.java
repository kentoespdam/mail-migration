package id.perumdamts.mail.dto.core.recipient;

import id.perumdamts.mail.dto.id.MailRecipientId;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecipientDeleteBatchRequest(
        @NotEmpty List<MailRecipientId> ids
) {}
