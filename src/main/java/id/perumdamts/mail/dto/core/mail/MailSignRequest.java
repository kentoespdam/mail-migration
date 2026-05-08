package id.perumdamts.mail.dto.core.mail;

import jakarta.validation.constraints.NotNull;

public record MailSignRequest(
        @NotNull(message = "Position ID wajib diisi")
        Long signerPosId
) {}