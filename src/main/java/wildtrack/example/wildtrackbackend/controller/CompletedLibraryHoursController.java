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
import java.util.stream.Collectors;

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
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String academicYear) {
        try {
            logger.info("Getting completed library hours with filters - gradeLevel: " + gradeLevel +
                    ", section: " + section +
                    ", subject: " + subject +
                    ", quarter: " + quarter +
                    ", dateFrom: " + dateFrom +
                    ", dateTo: " + dateTo +
                    ", academicYear: " + academicYear);

            // Get all students of the specified grade level and/or section
            List<User> students = new ArrayList<>();
            if (gradeLevel != null && !gradeLevel.isEmpty()) {
                if (section != null && !section.isEmpty()) {
                    // If both grade and section are specified, find students by both
                    students = userRepository.findByGradeAndSection(gradeLevel, section);
                    logger.info("Found " + students.size() + " students in grade level: " + gradeLevel +
                            ", section: " + section);
                } else {
                    // Otherwise just find by grade
                    students = userRepository.findByGrade(gradeLevel);
                    logger.info("Found " + students.size() + " students in grade level: " + gradeLevel);
                }
            } else if (section != null && !section.isEmpty()) {
                // Find by section only if grade is not specified
                students = userRepository.findBySection(section);
                logger.info("Found " + students.size() + " students in section: " + section);
            }

            // Get student IDs
            List<String> studentIds = new ArrayList<>();
            for (User student : students) {
                studentIds.add(student.getIdNumber());
            }

            // Get completed library hours based on filters
            List<LibraryRequirementProgress> completedProgress;

            try {
                // First, get all completed library hours
                completedProgress = progressRepository.findByIsCompletedTrue();
                logger.info("Found " + completedProgress.size() + " total completed library hours");

                // Apply subject filter if specified
                if (subject != null && !subject.isEmpty()) {
                    completedProgress = completedProgress.stream()
                            .filter(progress -> subject.equals(progress.getSubject()))
                            .collect(Collectors.toList());
                    logger.info("After subject filter: " + completedProgress.size() + " records");
                }

                // Apply quarter filter if specified
                if (quarter != null && !quarter.isEmpty()) {
                    completedProgress = completedProgress.stream()
                            .filter(progress -> quarter.equals(progress.getQuarter()))
                            .collect(Collectors.toList());
                    logger.info("After quarter filter: " + completedProgress.size() + " records");
                }

                // Apply date range filter if specified
                if (dateFrom != null && dateTo != null) {
                    completedProgress = completedProgress.stream()
                            .filter(progress -> {
                                if (progress.getLastUpdated() == null)
                                    return false;
                                return !progress.getLastUpdated().isBefore(dateFrom) &&
                                        !progress.getLastUpdated().isAfter(dateTo);
                            })
                            .collect(Collectors.toList());
                    logger.info("After date range filter: " + completedProgress.size() + " records");
                }
                // Apply academic year filter if specified
                else if (academicYear != null && !academicYear.isEmpty()) {
                    String[] years = academicYear.split("-");
                    if (years.length == 2) {
                        try {
                            int startYear = Integer.parseInt(years[0]);
                            int endYear = Integer.parseInt(years[1]);

                            // Filter by academic year pattern (July-Dec in first year, Jan-June in second
                            // year)
                            completedProgress = completedProgress.stream()
                                    .filter(progress -> {
                                        if (progress.getLastUpdated() == null)
                                            return false;

                                        LocalDate date = progress.getLastUpdated();
                                        int year = date.getYear();
                                        int month = date.getMonthValue();

                                        // Academic year pattern: Jul-Dec in first year, Jan-Jun in second year
                                        return (month >= 7 && month <= 12 && year == startYear) ||
                                                (month >= 1 && month <= 6 && year == endYear);
                                    })
                                    .collect(Collectors.toList());

                            logger.info("After academic year filter: " + completedProgress.size() + " records");
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid academic year format: " + academicYear);
                            // Fall back to exact match on academicYear field
                            completedProgress = completedProgress.stream()
                                    .filter(progress -> academicYear.equals(progress.getAcademicYear()))
                                    .collect(Collectors.toList());
                        }
                    } else {
                        // If format is not "YYYY-YYYY", try exact match
                        completedProgress = completedProgress.stream()
                                .filter(progress -> academicYear.equals(progress.getAcademicYear()))
                                .collect(Collectors.toList());
                    }
                }
            } catch (Exception e) {
                logger.warning("Error applying filters: " + e.getMessage() + ". Using basic filtering.");
                // Fallback to basic filtering
                completedProgress = progressRepository.findByIsCompletedTrue();
            }

            // Apply student filter if student IDs are available
            if (!studentIds.isEmpty()) {
                completedProgress = completedProgress.stream()
                        .filter(progress -> studentIds.contains(progress.getStudentId()))
                        .collect(Collectors.toList());
                logger.info("After student filter: " + completedProgress.size() + " records");
            }

            // Create response with user information
            List<Map<String, Object>> result = new ArrayList<>();
            for (LibraryRequirementProgress progress : completedProgress) {
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
                    record.put("section", student.getSection());

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