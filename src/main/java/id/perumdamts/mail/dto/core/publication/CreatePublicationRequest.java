package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.id.DocumentTypeId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePublicationRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private DocumentTypeId documentTypeId;
    private boolean publish;
    private MultipartFile file;
}
