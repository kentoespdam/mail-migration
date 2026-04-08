package id.perumdamts.mail.dto.core.recipient;

public record RecipientResponse(
                String id,
                String userId,
                String empId,
                String empName,
                String posName,
                String circulation,
                String circulationName,
                Integer emailNotif,
                Integer smsNotif,
                Boolean notified,
                Boolean read,
                Integer folderPosition) {
}
