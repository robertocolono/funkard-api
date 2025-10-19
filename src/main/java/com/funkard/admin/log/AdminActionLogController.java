package com.funkard.admin.log;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/history")
@RequiredArgsConstructor
public class AdminActionLogController {

    private final AdminActionLogRepository logRepository;

    @GetMapping("/{type}/{id}")
    public ResponseEntity<List<AdminActionLog>> getHistory(
            @PathVariable String type, @PathVariable Long id) {
        List<AdminActionLog> logs = logRepository.findByTargetIdAndTargetTypeOrderByCreatedAtAsc(id, type.toUpperCase());
        return ResponseEntity.ok(logs);
    }
}
