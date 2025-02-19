package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.TimeInService;
import wildtrack.example.wildtrackbackend.service.UserService;
import wildtrack.example.wildtrackbackend.entity.User;

import java.time.LocalDateTime;
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
    private UserRepository userRepository;

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

            for (int month = 1; month <= 12; month++) {
                LocalDateTime startOfMonth = LocalDateTime.of(currentYear, month, 1, 0, 0);
                LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

                // Count completed sessions (where timeOut is not null)
                long completedSessions = libraryHoursRepository
                        .countByTimeOutIsNotNullAndTimeInBetween(startOfMonth, endOfMonth);

                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", startOfMonth.getMonth().toString().substring(0, 3));
                monthData.put("participants", completedSessions);

                monthlyData.add(monthData);
            }

            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch participant statistics"));
        }
    }

}