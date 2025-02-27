package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.TimeInService;
import wildtrack.example.wildtrackbackend.service.UserService;
import wildtrack.example.wildtrackbackend.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "http://localhost:5173")
public class StatisticsController {

    @Autowired
    private TimeInService timeInService;

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            long studentsInsideLibrary = timeInService.getActiveStudentsCount();
            long totalRegisteredStudents = userService.getStudentsCount();

            return ResponseEntity.ok(Map.of(
                    "studentsInsideLibrary", studentsInsideLibrary,
                    "totalRegisteredStudents", totalRegisteredStudents));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch dashboard statistics"));
        }
    }

    @GetMapping("/active-participants")
    public ResponseEntity<?> getActiveParticipants() {
        try {
            // Get current year
            int currentYear = LocalDateTime.now().getYear();

            // Create response data for each month
            List<Map<String, Object>> monthlyData = new ArrayList<>();

            // Predefined list of months to ensure all months are included
            String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

            for (int month = 1; month <= 12; month++) {
                LocalDateTime startOfMonth = LocalDateTime.of(currentYear, month, 1, 0, 0);
                LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

                // Count completed sessions (where timeOut is not null)
                long completedSessions = libraryHoursRepository
                        .countByTimeOutIsNotNullAndTimeInBetween(startOfMonth, endOfMonth);

                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", months[month - 1]);
                monthData.put("participants", completedSessions);

                monthlyData.add(monthData);
            }

            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch participant statistics"));
        }
    }

    @GetMapping("/completion-rate")
    public ResponseEntity<?> getCompletionRate() {
        try {
            // Get current year
            int currentYear = LocalDate.now().getYear();

            // Create response data for each month
            List<Map<String, Object>> monthlyData = new ArrayList<>();

            for (int month = 1; month <= 12; month++) {
                LocalDate startOfMonth = LocalDate.of(currentYear, month, 1);
                LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

                // Count all requirements that were last updated in this month
                long totalRequirements = progressRepository.countByLastUpdatedBetween(
                        startOfMonth, endOfMonth);

                // Count completed requirements in this month
                long completedRequirements = progressRepository.countByIsCompletedTrueAndLastUpdatedBetween(
                        startOfMonth, endOfMonth);

                // Calculate completion rate
                double rate = totalRequirements > 0
                        ? (completedRequirements * 100.0 / totalRequirements)
                        : 0;

                Map<String, Object> monthData = new LinkedHashMap<>();
                monthData.put("month", Month.of(month).toString().substring(0, 3));
                monthData.put("rate", Math.round(rate));

                monthlyData.add(monthData);
            }

            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch completion rate statistics"));
        }
    }

    // Alternative approach focusing only on completed requirements by month
    @GetMapping("/completed-requirements")
    public ResponseEntity<?> getCompletedRequirements() {
        try {
            // Get current year
            int currentYear = LocalDate.now().getYear();

            // Create response data for each month
            List<Map<String, Object>> monthlyData = new ArrayList<>();

            for (int month = 1; month <= 12; month++) {
                LocalDate startOfMonth = LocalDate.of(currentYear, month, 1);
                LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

                // Count completed requirements for this month
                long completedRequirements = progressRepository.countByIsCompletedTrueAndLastUpdatedBetween(
                        startOfMonth, endOfMonth);

                Map<String, Object> monthData = new LinkedHashMap<>();
                monthData.put("month", Month.of(month).toString().substring(0, 3));
                monthData.put("rate", completedRequirements); // Using the actual count instead of percentage

                monthlyData.add(monthData);
            }

            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch completed requirements statistics"));
        }
    }
}