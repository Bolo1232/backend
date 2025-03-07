package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.TimeInService;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "http://localhost:5173")
public class StatisticsController {
    private static final Logger logger = Logger.getLogger(StatisticsController.class.getName());

    @Autowired
    private TimeInService timeInService;

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStatistics(
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String section) {
        try {
            // Get students who are currently inside the library
            // In a real implementation, this might also need filtering by grade/section
            long studentsInsideLibrary = timeInService.getActiveStudentsCount();

            // Get total registered students filtered by grade level and section
            long totalRegisteredStudents;
            if (gradeLevel != null || section != null) {
                // If filters are provided, use the filtered count method
                totalRegisteredStudents = userService.getStudentsCountByGradeAndSection(gradeLevel, section);
            } else {
                // If no filters, use the total count method
                totalRegisteredStudents = userService.getStudentsCount();
            }

            return ResponseEntity.ok(Map.of(
                    "studentsInsideLibrary", studentsInsideLibrary,
                    "totalRegisteredStudents", totalRegisteredStudents));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch dashboard statistics"));
        }
    }

    @GetMapping("/active-participants")
    public ResponseEntity<?> getActiveParticipants(
            @RequestParam(required = false) String timeframe,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String subject) {
        try {
            logger.info("Getting active participants with filters: " +
                    "timeframe=" + timeframe +
                    ", gradeLevel=" + gradeLevel +
                    ", section=" + section +
                    ", academicYear=" + academicYear +
                    ", quarter=" + quarter +
                    ", subject=" + subject);

            // Default to monthly if timeframe not specified
            timeframe = (timeframe != null) ? timeframe.toLowerCase() : "monthly";

            // Get current date and year
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();

            // Create response data
            List<Map<String, Object>> resultData = new ArrayList<>();

            // Apply grade level and section filters to get relevant users
            List<User> filteredUsers = filterUsers(gradeLevel, section, academicYear);
            List<String> userIds = filteredUsers.stream()
                    .map(User::getIdNumber)
                    .collect(Collectors.toList());

            if ("weekly".equals(timeframe)) {
                // For weekly data - show days of the week for the current week
                LocalDate monday = now.toLocalDate().with(DayOfWeek.MONDAY);

                // If today is Sunday, use this week's Monday
                if (now.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    monday = monday.minusWeeks(1);
                }

                // Create data for each day of the week
                for (int i = 0; i < 7; i++) {
                    LocalDate currentDay = monday.plusDays(i);
                    String dayName = currentDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                    // Define time boundaries for the day
                    LocalDateTime startOfDay = currentDay.atStartOfDay();
                    LocalDateTime endOfDay = currentDay.plusDays(1).atStartOfDay().minusNanos(1);

                    // Count unique participants for this day with quarter and subject filtering
                    long uniqueParticipants = countUniqueParticipants(
                            userIds, startOfDay, endOfDay, quarter, subject);

                    Map<String, Object> dayData = new LinkedHashMap<>();
                    dayData.put("day", dayName);
                    dayData.put("participants", uniqueParticipants);

                    resultData.add(dayData);
                }
            } else {
                // For monthly data - show all months in current year
                // Predefined list of months to ensure all months are included
                String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

                for (int month = 1; month <= 12; month++) {
                    LocalDateTime startOfMonth = LocalDateTime.of(currentYear, month, 1, 0, 0);
                    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

                    // Count unique participants for this month with quarter and subject filtering
                    long uniqueParticipants = countUniqueParticipants(
                            userIds, startOfMonth, endOfMonth, quarter, subject);

                    Map<String, Object> monthData = new LinkedHashMap<>();
                    monthData.put("month", months[month - 1]);
                    monthData.put("participants", uniqueParticipants);

                    resultData.add(monthData);
                }
            }

            return ResponseEntity.ok(resultData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch participant statistics: " + e.getMessage()));
        }
    }

    /**
     * Count unique participants with subject and quarter filtering
     */
    private long countUniqueParticipants(
            List<String> userIds,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String quarter,
            String subject) {

        // If no quarter or subject filter, use the existing repository method
        if ((quarter == null || quarter.isEmpty()) && (subject == null || subject.isEmpty())) {
            if (userIds.isEmpty()) {
                return libraryHoursRepository.countDistinctUsersByTimeInBetween(startTime, endTime);
            } else {
                return libraryHoursRepository.countDistinctUsersByIdNumberInAndTimeInBetween(
                        userIds, startTime, endTime);
            }
        }

        // Convert to LocalDate for progress repository methods
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        // For quarter and subject filtering, we need to count based on progress
        // repository
        Set<String> uniqueParticipants = new HashSet<>();

        // Base list of users to check
        List<String> idsToCheck = userIds.isEmpty() ? userRepository.findByRole("Student").stream()
                .map(User::getIdNumber)
                .collect(Collectors.toList()) : userIds;

        // For each user, check if they have progress matching our criteria
        for (String userId : idsToCheck) {
            // Get all progress records for this student
            List<LibraryRequirementProgress> progressList = progressRepository.findByStudentId(userId);

            // Check if any progress record meets our filtering criteria
            boolean hasMatchingProgress = false;

            if (quarter != null && !quarter.isEmpty() && subject != null && !subject.isEmpty()) {
                // Both quarter and subject filters
                hasMatchingProgress = progressList.stream()
                        .anyMatch(progress -> progress.getQuarter().equals(quarter) &&
                                progress.getSubject().equals(subject) &&
                                progress.getMinutesRendered() > 0 &&
                                progress.getLastUpdated() != null &&
                                !progress.getLastUpdated().isBefore(startDate) &&
                                !progress.getLastUpdated().isAfter(endDate));
            } else if (quarter != null && !quarter.isEmpty()) {
                // Only quarter filter
                hasMatchingProgress = progressList.stream()
                        .anyMatch(progress -> progress.getQuarter().equals(quarter) &&
                                progress.getMinutesRendered() > 0 &&
                                progress.getLastUpdated() != null &&
                                !progress.getLastUpdated().isBefore(startDate) &&
                                !progress.getLastUpdated().isAfter(endDate));
            } else if (subject != null && !subject.isEmpty()) {
                // Only subject filter
                hasMatchingProgress = progressList.stream()
                        .anyMatch(progress -> progress.getSubject().equals(subject) &&
                                progress.getMinutesRendered() > 0 &&
                                progress.getLastUpdated() != null &&
                                !progress.getLastUpdated().isBefore(startDate) &&
                                !progress.getLastUpdated().isAfter(endDate));
            }

            // If the user has matching progress, count them as a participant
            if (hasMatchingProgress) {
                uniqueParticipants.add(userId);
            }
        }

        return uniqueParticipants.size();
    }

    @GetMapping("/completion-rate")
    public ResponseEntity<?> getCompletionRate(
            @RequestParam(required = false) String timeframe,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String subject) {
        try {
            logger.info("Getting completion rate with filters: " +
                    "timeframe=" + timeframe +
                    ", gradeLevel=" + gradeLevel +
                    ", section=" + section +
                    ", academicYear=" + academicYear +
                    ", subject=" + subject);

            // Default to monthly if timeframe not specified
            timeframe = (timeframe != null) ? timeframe.toLowerCase() : "monthly";

            // Get current year and week
            LocalDate today = LocalDate.now();
            int currentYear = today.getYear();

            // Apply grade level and section filters to get relevant users
            List<User> filteredUsers = filterUsers(gradeLevel, section, academicYear);
            // Get student IDs for filtering
            List<String> studentIds = filteredUsers.stream()
                    .map(User::getIdNumber)
                    .collect(Collectors.toList());

            logger.info("Filtered to " + studentIds.size() + " students for completion rate");

            List<Map<String, Object>> resultData = new ArrayList<>();

            if ("weekly".equals(timeframe)) {
                // Find the Monday of the current week
                LocalDate monday = today.with(DayOfWeek.MONDAY);

                // If today is Sunday, use this week's Monday (not last week's)
                if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    monday = monday.minusWeeks(1);
                }

                // Create a separate map for each day of the week
                Map<DayOfWeek, Map<String, Object>> weekDataMap = new HashMap<>();

                // Initialize all days with zero values
                for (DayOfWeek dow : DayOfWeek.values()) {
                    Map<String, Object> dayData = new LinkedHashMap<>();
                    dayData.put("day", dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                    dayData.put("rate", 0); // Default to zero
                    weekDataMap.put(dow, dayData);
                }

                // Create a fixed set of 7 days for the current week
                for (int i = 0; i < 7; i++) {
                    LocalDate currentDate = monday.plusDays(i);
                    DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

                    // Calculate completion rate for this day using the filtered student IDs
                    double rate = calculateCompletionRate(currentDate, currentDate, studentIds, subject);

                    // Update the map with actual data
                    weekDataMap.get(dayOfWeek).put("rate", Math.round(rate));
                }

                // Add days in order from Monday to Sunday
                DayOfWeek[] orderedDays = {
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                };

                for (DayOfWeek day : orderedDays) {
                    resultData.add(weekDataMap.get(day));
                }
            } else {
                // For monthly data - show all months in current year
                for (int month = 1; month <= 12; month++) {
                    LocalDate startOfMonth = LocalDate.of(currentYear, month, 1);
                    LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

                    // Calculate completion rate for this month using the filtered student IDs
                    double rate = calculateCompletionRate(startOfMonth, endOfMonth, studentIds, subject);

                    Map<String, Object> monthData = new LinkedHashMap<>();
                    monthData.put("month", Month.of(month).toString().substring(0, 3));
                    monthData.put("rate", Math.round(rate));

                    resultData.add(monthData);
                }
            }

            return ResponseEntity.ok(resultData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch completion rate statistics: " + e.getMessage()));
        }
    }

    /**
     * Calculate the completion rate for a specific date range, filtered by student
     * IDs and subject
     */
    private double calculateCompletionRate(
            LocalDate startDate,
            LocalDate endDate,
            List<String> studentIds,
            String subject) {

        long totalRequirements = 0;
        long completedRequirements = 0;

        // If no students to filter by, return 0
        if (studentIds.isEmpty()) {
            return 0;
        }

        // Get all progress records for the filtered students within the date range
        List<LibraryRequirementProgress> progressList = new ArrayList<>();

        for (String studentId : studentIds) {
            List<LibraryRequirementProgress> studentProgress = progressRepository.findByStudentId(studentId);

            // Filter by date range
            List<LibraryRequirementProgress> filteredProgress = studentProgress.stream()
                    .filter(p -> p.getLastUpdated() != null &&
                            !p.getLastUpdated().isBefore(startDate) &&
                            !p.getLastUpdated().isAfter(endDate))
                    .collect(Collectors.toList());

            progressList.addAll(filteredProgress);
        }

        // Apply subject filter if specified
        if (subject != null && !subject.isEmpty()) {
            progressList = progressList.stream()
                    .filter(p -> subject.equals(p.getSubject()))
                    .collect(Collectors.toList());
        }

        // Count total and completed requirements
        totalRequirements = progressList.size();
        completedRequirements = progressList.stream()
                .filter(LibraryRequirementProgress::getIsCompleted)
                .count();

        // Calculate completion rate
        double rate = totalRequirements > 0
                ? (completedRequirements * 100.0 / totalRequirements)
                : 0;

        return rate;
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

    // Make method public to fix visibility issue
    public List<User> filterUsers(String gradeLevel, String section, String academicYear) {
        List<User> allUsers = userRepository.findByRole("Student");

        // Log the filter parameters for debugging
        logger.info("Filtering users with criteria: gradeLevel=" + gradeLevel +
                ", section=" + section + ", academicYear=" + academicYear);

        // Apply filters with enhanced null handling
        return allUsers.stream()
                .filter(user -> gradeLevel == null || gradeLevel.isEmpty() || "All Grades".equals(gradeLevel) ||
                        gradeLevel.equals(user.getGrade()) ||
                        gradeLevel.equals("Grade " + user.getGrade()))
                .filter(user -> section == null || section.isEmpty() ||
                        section.equals(user.getSection()))
                .filter(user -> academicYear == null || academicYear.isEmpty() ||
                        academicYear.equals(user.getAcademicYear()))
                .collect(Collectors.toList());
    }
}