package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/library-hours")

public class CompletedLibraryHoursController {
    private static final Logger logger = Logger.getLogger(CompletedLibraryHoursController.class.getName());

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get completed library hours with filtering options
     */
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedLibraryHours(
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String academicYear) {
        try {
            logger.info("Getting completed library hours with filters - gradeLevel: " + gradeLevel +
                    ", subject: " + subject +
                    ", quarter: " + quarter +
                    ", dateFrom: " + dateFrom +
                    ", dateTo: " + dateTo +
                    ", academicYear: " + academicYear);

            // Get all students of the specified grade level
            List<User> students = new ArrayList<>();
            if (gradeLevel != null && !gradeLevel.isEmpty()) {
                students = userRepository.findByGrade(gradeLevel);
                logger.info("Found " + students.size() + " students in grade level: " + gradeLevel);
            }

            // Get student IDs
            List<String> studentIds = new ArrayList<>();
            for (User student : students) {
                studentIds.add(student.getIdNumber());
            }

            // Get completed library hours based on filters
            List<LibraryRequirementProgress> completedProgress;

            try {
                // Apply repository-level filtering first
                if (subject != null && !subject.isEmpty() && quarter != null && !quarter.isEmpty()) {
                    completedProgress = progressRepository.findBySubjectAndQuarterAndIsCompletedTrue(subject, quarter);
                } else if (subject != null && !subject.isEmpty()) {
                    completedProgress = progressRepository.findBySubjectAndIsCompletedTrue(subject);
                } else if (quarter != null && !quarter.isEmpty()) {
                    completedProgress = progressRepository.findByQuarterAndIsCompletedTrue(quarter);
                } else if (academicYear != null && !academicYear.isEmpty()) {
                    completedProgress = progressRepository.findByAcademicYearAndIsCompletedTrue(academicYear);
                } else if (dateFrom != null && dateTo != null) {
                    completedProgress = progressRepository.findByIsCompletedTrueAndLastUpdatedBetween(dateFrom, dateTo);
                } else {
                    completedProgress = progressRepository.findByIsCompletedTrue();
                }
            } catch (Exception e) {
                logger.warning(
                        "Error using repository methods: " + e.getMessage() + ". Falling back to manual filtering.");
                // Fallback to manual filtering if the repository methods fail
                completedProgress = progressRepository.findAll().stream()
                        .filter(progress -> progress.getIsCompleted())
                        .toList();
            }

            // Create response with user information
            List<Map<String, Object>> result = new ArrayList<>();
            for (LibraryRequirementProgress progress : completedProgress) {
                // Skip if grade level filter is applied and student is not in the grade level
                if (!studentIds.isEmpty() && !studentIds.contains(progress.getStudentId())) {
                    continue;
                }

                // Apply any additional filters manually if needed
                if ((dateFrom != null
                        && (progress.getLastUpdated() == null || progress.getLastUpdated().isBefore(dateFrom))) ||
                        (dateTo != null
                                && (progress.getLastUpdated() == null || progress.getLastUpdated().isAfter(dateTo)))
                        ||
                        (subject != null && !subject.isEmpty() && !subject.equals(progress.getSubject())) ||
                        (quarter != null && !quarter.isEmpty() && !quarter.equals(progress.getQuarter())) ||
                        (academicYear != null && !academicYear.isEmpty() &&
                                (progress.getAcademicYear() == null
                                        || !academicYear.equals(progress.getAcademicYear())))) {
                    continue;
                }

                // Get user details
                Optional<User> userOpt = userRepository.findByIdNumber(progress.getStudentId());
                if (userOpt.isPresent()) {
                    User student = userOpt.get();

                    Map<String, Object> record = new HashMap<>();
                    record.put("idNumber", student.getIdNumber());
                    record.put("name", student.getFirstName() + " " + student.getLastName());
                    record.put("subject", progress.getSubject());
                    record.put("quarter", progress.getQuarter());
                    record.put("gradeLevel", student.getGrade());

                    // Format date completed (last updated date)
                    if (progress.getLastUpdated() != null) {
                        record.put("dateCompleted", progress.getLastUpdated().toString());
                    }

                    result.add(record);
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Error getting completed library hours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch completed library hours: " + e.getMessage()));
        }
    }
}