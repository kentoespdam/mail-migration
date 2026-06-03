package id.perumdamts.mail.controller.me;

import id.perumdamts.mail.dto.me.PositionResponse;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.me.UserPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserPositionController {
    private final UserPositionService service;

    @GetMapping("/positions")
    public List<PositionResponse> getPositions(@AuthenticationPrincipal MailPrincipal principal) {
        return service.getPositionsForUser(principal);
    }
}
