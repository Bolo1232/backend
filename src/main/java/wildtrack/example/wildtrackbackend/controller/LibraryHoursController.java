package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.UserService;
import wildtrack.example.wildtrackbackend.dto.StudentLibrarySummary;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library-hours")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for frontend
public class LibraryHoursController {

    @Autowired
    private LibraryHoursService libraryHoursService;

    @Autowired
    private UserService userService;

    @GetMapping("/analytics/average-minutes")
    public ResponseEntity<Map<String, Double>> getAverageMinutes() {
        try {
            double averageMinutes = libraryHoursService.calculateAverageMinutes();
            // Ensure the response type explicitly matches Map<String, Double>
            return ResponseEntity.ok(Map.of("averageMinutes", averageMinutes));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // null here because a map cannot hold error messages
        }
    }

    // API to get Accession Usage Frequency
    @GetMapping("/analytics/accession-usage")
    public ResponseEntity<Map<String, Long>> getAccessionUsageFrequency() {
        try {
            Map<String, Long> accessionFrequency = libraryHoursService.calculateAccessionUsageFrequency();
            return ResponseEntity.ok(accessionFrequency);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/time-in")
    public ResponseEntity<?> recordTimeIn(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        try {
            User user = userService.findByIdNumber(idNumber);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Student not found."));
            }

            libraryHoursService.recordTimeIn(idNumber);
            return ResponseEntity.ok(Map.of("message", "Time-in recorded successfully.", "student", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/time-out")
    public ResponseEntity<?> recordTimeOut(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        try {
            User user = userService.findByIdNumber(idNumber);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Student not found."));
            }

            libraryHoursService.recordTimeOut(idNumber);

            return ResponseEntity.ok(Map.of("message", "Time-out recorded successfully.", "student", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

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
        List<StudentLibrarySummary> summaries = libraryHoursService.getLibraryHoursSummary();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LibraryHours>> getAllLibraryHours() {
        try {
            List<LibraryHours> libraryHours = libraryHoursService.getAllLibraryHours();
            return ResponseEntity.ok(libraryHours);
        } catch (Exception e) {
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

}
