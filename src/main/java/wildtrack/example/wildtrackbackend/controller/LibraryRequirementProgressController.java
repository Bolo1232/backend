package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.service.LibraryRequirementProgressService;

@RestController
@RequestMapping("/api/library-progress")
public class LibraryRequirementProgressController {

    @Autowired
    private LibraryRequirementProgressService progressService;

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    /**
     * Get all requirement progress for a student
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentProgress(@PathVariable String studentId) {
        try {
            List<LibraryRequirementProgress> progress = progressService.getStudentProgress(studentId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving requirement progress: " + e.getMessage()));
        }
    }

    /**
     * Get progress summary for a student (without auto-initialization)
     */
    @GetMapping("/summary/{studentId}")
    public ResponseEntity<?> getProgressSummary(@PathVariable String studentId) {
        try {
            Map<String, Object> summary = progressService.getProgressSummary(studentId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving progress summary: " + e.getMessage()));
        }
    }

    /**
     * Get progress summary for a student with auto-initialization
     * This will initialize requirements if none exist
     */
    @GetMapping("/summary-with-init/{studentId}")
    public ResponseEntity<?> getProgressSummaryWithInit(@PathVariable String studentId) {
        try {
            Map<String, Object> summary = progressService.getProgressSummaryWithInit(studentId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving progress summary: " + e.getMessage()));
        }
    }

    /**
     * Get progress with active status indicating if student is currently working on
     * it
     */
    @GetMapping("/active-progress/{studentId}")
    public ResponseEntity<?> getActiveProgressStatus(@PathVariable String studentId) {
        try {
            List<Map<String, Object>> progress = progressService.getActiveProgressWithTimingStatus(studentId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving active progress: " + e.getMessage()));
        }
    }

    /**
     * Check for new requirements added by teachers
     */
    @GetMapping("/check-new-requirements/{studentId}")
    public ResponseEntity<?> checkForNewRequirements(@PathVariable String studentId) {
        try {
            List<LibraryRequirementProgress> updatedRequirements = progressService.checkForNewRequirements(studentId);
            return ResponseEntity.ok(updatedRequirements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error checking for new requirements: " + e.getMessage()));
        }
    }

    /**
     * Force refresh requirements for a student to get any new requirements
     */
    @PostMapping("/refresh/{studentId}")
    public ResponseEntity<?> refreshRequirements(@PathVariable String studentId) {
        try {
            // Force re-initialize requirements
            progressService.initializeRequirements(studentId);

            // Get updated progress
            List<LibraryRequirementProgress> progress = progressService.getStudentProgress(studentId);

            return ResponseEntity.ok(Map.of(
                    "message", "Requirements refreshed successfully",
                    "requirements", progress));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error refreshing requirements: " + e.getMessage()));
        }
    }

    /**
     * Get requirement details with contributing sessions
     */
    @GetMapping("/requirement-details/{requirementId}")
    public ResponseEntity<?> getRequirementDetails(
            @PathVariable Long requirementId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            // Get the requirement details with contributing sessions
            Map<String, Object> details = progressService.getRequirementDetailsWithContributingSessions(requirementId,
                    page, size);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving requirement details: " + e.getMessage()));
        }
    }

    /**
     * Run migration for existing library hours data
     */
    @PostMapping("/migrate-library-hours")
    public ResponseEntity<?> migrateLibraryHours() {
        try {
            // Run migration
            progressService.migrateExistingLibraryHoursToRequirements();

            return ResponseEntity.ok(Map.of("message", "Migration completed successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error during migration: " + e.getMessage()));
        }
    }
}