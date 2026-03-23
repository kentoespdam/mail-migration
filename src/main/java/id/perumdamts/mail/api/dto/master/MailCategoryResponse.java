package id.perumdamts.mail.api.dto.master;

public record MailCategoryResponse(
        Integer id,
        Integer mailTypeId,
        String mailTypeName,
        String code,
        String name,
        String codeName,
        String status,
        Integer sort
) {}
