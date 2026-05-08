package id.perumdamts.mail.dto.me;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {
    private Long posId;
    private String posName;
    private String unitName;
    private boolean isPlt;
    private boolean isActive;
}
