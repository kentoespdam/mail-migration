package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.response.ResponseTimeAggregateDto;
import id.perumdamts.mail.dto.core.response.ResponseTimeFilterRequest;
import id.perumdamts.mail.service.core.response.ResponseTimeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/response-time")
@RequiredArgsConstructor
public class ResponseTimeController {

    private final ResponseTimeQueryService responseTimeQueryService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseTimeAggregateDto> getStats(
            @RequestParam(required = false) Long mailTypeId,
            @RequestParam(required = false) Long mailCategoryId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ResponseTimeFilterRequest filter = new ResponseTimeFilterRequest(
                mailTypeId,
                mailCategoryId,
                unitId,
                startDate,
                endDate
        );

        return ResponseEntity.ok(responseTimeQueryService.aggregate(filter));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseTimeAggregateDto> getDefaultStats() {
        return ResponseEntity.ok(responseTimeQueryService.getDefaultStats());
    }
}