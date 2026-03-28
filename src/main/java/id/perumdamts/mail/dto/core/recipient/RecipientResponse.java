package id.perumdamts.mail.dto.core.recipient;

public record RecipientResponse(
        Long id,
        Integer userId,
        Integer empId,
        String empName,
        String posName,
        Integer circulation,
        String circulationName,
        Integer emailNotif,
        Integer smsNotif,
        Boolean notified,
        Boolean read,
        Integer folderPosition
) {}
