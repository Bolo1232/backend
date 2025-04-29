package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.dto.StudentLibrarySummary;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;

@RestController
@RequestMapping("/api/library-hours")
public class LibraryHoursController {

    @Autowired
    private LibraryHoursService libraryHoursService;

    @GetMapping("/user/{idNumber}")
    public ResponseEntity<?> getLibraryHoursByUser(@PathVariable String idNumber) {
        try {
            List<LibraryHours> libraryHours = libraryHoursService.getLibraryHoursByIdNumber(idNumber);
            return ResponseEntity.ok(libraryHours);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching library hours."));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<List<StudentLibrarySummary>> getLibraryHoursSummary() {
        try {
            List<StudentLibrarySummary> summaries = libraryHoursService.getLibraryHoursSummary();
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<LibraryHours>> getAllLibraryHours() {
        try {
            List<LibraryHours> libraryHours = libraryHoursService.getAllLibraryHours();
            return ResponseEntity.ok(libraryHours);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/with-user-details")
    public ResponseEntity<?> getAllLibraryHoursWithUserDetails() {
        try {
            List<Map<String, Object>> response = libraryHoursService.getAllLibraryHoursWithUserDetails();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching library hours."));
        }
    }

    // Modified to also handle requiresBookAssignment flag
    @PutMapping("/{id}/add-summary")
    public ResponseEntity<?> addSummaryToLibraryHours(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("summary")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Summary is required"));
            }

            String summary = (String) payload.get("summary");

            // Check if requiresBookAssignment flag is provided
            Boolean requiresBookAssignment = payload.containsKey("requiresBookAssignment")
                    ? (Boolean) payload.get("requiresBookAssignment")
                    : false; // Default to false if not provided

            LibraryHours libraryHours = libraryHoursService.getLibraryHoursById(id)
                    .orElseThrow(() -> new RuntimeException("LibraryHours record not found."));

            libraryHours.setSummary(summary);
            libraryHours.setRequiresBookAssignment(requiresBookAssignment);
            LibraryHours updated = libraryHoursService.saveLibraryHours(libraryHours);

            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // New endpoint to update flags for library hours
    @PutMapping("/{id}/update-flags")
    public ResponseEntity<?> updateLibraryHoursFlags(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean requiresBookAssignment = payload.get("requiresBookAssignment");

            if (requiresBookAssignment == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "requiresBookAssignment flag is required"));
            }

            LibraryHours libraryHours = libraryHoursService.getLibraryHoursById(id)
                    .orElseThrow(() -> new RuntimeException("LibraryHours record not found."));

            libraryHours.setRequiresBookAssignment(requiresBookAssignment);
            LibraryHours updated = libraryHoursService.saveLibraryHours(libraryHours);

            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PutMapping("/update-summary")
    public ResponseEntity<?> updateStudentLibrarySummary(@RequestBody StudentLibrarySummary summaryUpdate) {
        try {
            if (summaryUpdate.getIdNumber() == null || summaryUpdate.getIdNumber().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID Number is required for updating the record."));
            }

            StudentLibrarySummary updatedSummary = libraryHoursService.updateStudentLibrarySummary(
                    summaryUpdate.getIdNumber(), summaryUpdate);

            return ResponseEntity.ok(updatedSummary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}