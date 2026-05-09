package id.perumdamts.mail.dto.me;

import id.perumdamts.mail.dto.id.PositionId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {
    private PositionId posId;
    private String posName;
    private String unitName;
    private boolean isPlt;
    private boolean isActive;
}
