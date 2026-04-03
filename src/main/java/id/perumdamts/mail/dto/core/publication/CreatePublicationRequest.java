package id.perumdamts.mail.dto.core.publication;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePublicationRequest {
    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String documentTypeId;
    private boolean publish;
    private MultipartFile file;
}
