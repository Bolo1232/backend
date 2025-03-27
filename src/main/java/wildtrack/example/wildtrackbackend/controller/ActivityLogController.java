package wildtrack.example.wildtrackbackend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.ActivityLog;
import wildtrack.example.wildtrackbackend.service.ActivityLogService;

@RestController
@RequestMapping("/api/activity-logs")

public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Get all activity logs with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<ActivityLog>> getActivityLogs(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String query) {

        // Verify user authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // Fetch activity logs with filters
        List<ActivityLog> logs = activityLogService.getActivityLogs(
                academicYear,
                dateFrom,
                dateTo,
                query);

        return ResponseEntity.ok(logs);
    }

    /**
     * Get all activity logs - simplified endpoint
     */
    @GetMapping("/all")
    public ResponseEntity<List<ActivityLog>> getAllActivityLogs() {
        // Verify user authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(activityLogService.getAllActivityLogs());
    }
}