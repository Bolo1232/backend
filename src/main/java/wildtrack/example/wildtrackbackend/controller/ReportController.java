package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.Report;
import wildtrack.example.wildtrackbackend.service.ReportService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")

public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitReport(@RequestBody Report report) {
        try {
            // Set submission time to now
            report.setDateSubmitted(LocalDateTime.now());

            // Set initial status if not provided
            if (report.getStatus() == null || report.getStatus().isEmpty()) {
                report.setStatus("Pending");
            }

            Report savedReport = reportService.saveReport(report);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit report: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllReports() {
        try {
            List<Report> reports = reportService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch reports: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReportsByUserId(@PathVariable Long userId) {
        try {
            List<Report> reports = reportService.getReportsByUserId(userId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch reports: " + e.getMessage()));
        }
    }

    @GetMapping("/user/idNumber/{idNumber}")
    public ResponseEntity<?> getReportsByUserIdNumber(@PathVariable String idNumber) {
        try {
            List<Report> reports = reportService.getReportsByUserIdNumber(idNumber);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch reports: " + e.getMessage()));
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId) {
        try {
            Report report = reportService.getReportById(reportId);
            if (report == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Report not found with ID: " + reportId));
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch report: " + e.getMessage()));
        }
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId, @RequestBody Report reportDetails) {
        try {
            Report updatedReport = reportService.updateReport(reportId, reportDetails);
            if (updatedReport == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Report not found with ID: " + reportId));
            }
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update report: " + e.getMessage()));
        }
    }

    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long reportId, @RequestBody Map<String, String> resolution) {
        try {
            String adminComments = resolution.get("adminComments");
            Report resolvedReport = reportService.resolveReport(reportId, adminComments);
            if (resolvedReport == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Report not found with ID: " + reportId));
            }
            return ResponseEntity.ok(resolvedReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to resolve report: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        try {
            boolean deleted = reportService.deleteReport(reportId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Report not found with ID: " + reportId));
            }
            return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete report: " + e.getMessage()));
        }
    }
}