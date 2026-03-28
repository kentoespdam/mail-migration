package id.perumdamts.mail.dto.core.recipient;

import java.util.List;

public record BatchRecipientResponse(
        List<RecipientResponse> succeeded,
        List<FailedRecipient> failed,
        int totalRequested,
        int totalSucceeded,
        int totalFailed
) {

    public record FailedRecipient(
            Integer empId,
            String reason
    ) {}

    public static BatchRecipientResponse of(List<RecipientResponse> succeeded, List<FailedRecipient> failed,
                                              int totalRequested) {
        return new BatchRecipientResponse(succeeded, failed, totalRequested, succeeded.size(), failed.size());
    }
}
