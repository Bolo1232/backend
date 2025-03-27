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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")

public class StatisticsController {
    private static final Logger logger = Logger.getLogger(StatisticsController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            logger.info("Getting active participants with filters: " +
                    "timeframe=" + timeframe +
                    ", gradeLevel=" + gradeLevel +
                    ", section=" + section +
                    ", academicYear=" + academicYear +
                    ", quarter=" + quarter +
                    ", subject=" + subject +
                    ", dateFrom=" + dateFrom +
                    ", dateTo=" + dateTo);

            // Default to monthly if timeframe not specified
            timeframe = (timeframe != null) ? timeframe.toLowerCase() : "monthly";

            // Get current date and year
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();

            // Parse date filters if provided
            LocalDate fromDate = null;
            LocalDate toDate = null;

            // If academic year is specified (format should be like "2025-2026")
            if (academicYear != null && !academicYear.isEmpty()) {
                try {
                    // Parse the academic year range
                    String[] years = academicYear.split("-");
                    if (years.length == 2) {
                        int startYear = Integer.parseInt(years[0]);
                        int endYear = Integer.parseInt(years[1]);

                        // If no explicit date range is provided, set it based on academic year
                        if (dateFrom == null || dateFrom.isEmpty()) {
                            // Typically academic years start in August/September
                            fromDate = LocalDate.of(startYear, Month.JULY, 1);
                        }

                        if (dateTo == null || dateTo.isEmpty()) {
                            // Typically academic years end in May/June
                            toDate = LocalDate.of(endYear, Month.JUNE, 30);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to parse academic year: " + academicYear);
                    // Continue with default values if parsing fails
                }
            }

            // Explicit date filters override academic year settings
            if (dateFrom != null && !dateFrom.isEmpty()) {
                fromDate = LocalDate.parse(dateFrom, DATE_FORMATTER);
            }

            if (dateTo != null && !dateTo.isEmpty()) {
                toDate = LocalDate.parse(dateTo, DATE_FORMATTER);
            }

            // Create response data
            List<Map<String, Object>> resultData = new ArrayList<>();

            // Apply grade level and section filters to get relevant users
            // Important: We don't filter by academicYear property, only by grade and
            // section
            List<User> filteredUsers = filterUsers(gradeLevel, section);
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

                // Adjust date range if using date filters
                LocalDate startDay = fromDate != null ? fromDate : monday;
                LocalDate endDay = toDate != null ? toDate : monday.plusDays(6);

                // Create data for each day in the range
                LocalDate currentDay = startDay;
                while (!currentDay.isAfter(endDay)) {
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
                    currentDay = currentDay.plusDays(1);
                }
            } else {
                // For monthly data
                if (fromDate == null && toDate == null) {
                    // If no date filters and no academic year, default to current year
                    fromDate = LocalDate.of(currentYear, 1, 1);
                    toDate = LocalDate.of(currentYear, 12, 31);
                }

                // Ensure we have both dates
                LocalDate startDate = fromDate != null ? fromDate : LocalDate.of(currentYear, 1, 1);
                LocalDate endDate = toDate != null ? toDate : LocalDate.of(currentYear, 12, 31);

                // Predefined list of months
                String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

                // Create a range of year-month combinations to iterate through
                YearMonth start = YearMonth.from(startDate);
                YearMonth end = YearMonth.from(endDate);

                YearMonth current = start;
                while (!current.isAfter(end)) {
                    int year = current.getYear();
                    int month = current.getMonthValue();

                    // Get start and end of month
                    LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
                    LocalDateTime endOfMonth = current.atEndOfMonth().atTime(23, 59, 59);

                    // Create adjusted time bounds that respect the specific date range
                    LocalDateTime adjustedStartTime = startOfMonth;
                    LocalDateTime adjustedEndTime = endOfMonth;

                    // If fromDate is provided and is in this month, use it as the start
                    if (fromDate != null && fromDate.getYear() == year && fromDate.getMonthValue() == month) {
                        adjustedStartTime = fromDate.atStartOfDay();
                    }

                    // If dateTo is provided and is in this month, use it as the end
                    if (toDate != null && toDate.getYear() == year && toDate.getMonthValue() == month) {
                        adjustedEndTime = toDate.atTime(23, 59, 59);
                    }

                    // Count unique participants for this month with quarter and subject filtering
                    // using adjusted time bounds to respect the specific date range
                    long uniqueParticipants = countUniqueParticipants(
                            userIds, adjustedStartTime, adjustedEndTime, quarter, subject);

                    Map<String, Object> monthData = new LinkedHashMap<>();

                    // Include year in label if spanning multiple years
                    String monthLabel = months[month - 1];
                    if (start.getYear() != end.getYear()) {
                        monthLabel += " " + year;
                    }

                    monthData.put("month", monthLabel);
                    monthData.put("participants", uniqueParticipants);

                    resultData.add(monthData);

                    // Move to next month
                    current = current.plusMonths(1);
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
     * Count unique participants with proper combination of all filters
     */
    private long countUniqueParticipants(
            List<String> userIds,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String quarter,
            String subject) {

        // Convert to LocalDate for progress repository methods
        final LocalDate startDate = startTime.toLocalDate();
        final LocalDate endDate = endTime.toLocalDate();
        final String finalQuarter = quarter;
        final String finalSubject = subject;

        // For filtering, we need to use progress repository
        Set<String> uniqueParticipants = new HashSet<>();

        // Base list of users to check (already filtered by grade and section only)
        List<String> idsToCheck = userIds.isEmpty() ? userRepository.findByRole("Student").stream()
                .map(User::getIdNumber)
                .collect(Collectors.toList())
                : userIds;

        // For each user, check if they have progress matching ALL filtering criteria
        for (String userId : idsToCheck) {
            List<LibraryRequirementProgress> progressList = progressRepository.findByStudentId(userId);

            // Create a combined filter that applies ALL conditions
            boolean hasMatchingProgress = progressList.stream()
                    .anyMatch(progress -> {
                        // Date range condition - this already incorporates academic year if set
                        boolean dateRangeMatches = progress.getLastUpdated() != null &&
                                !progress.getLastUpdated().isBefore(startDate) &&
                                !progress.getLastUpdated().isAfter(endDate);

                        // Minutes condition
                        boolean minutesMatch = progress.getMinutesRendered() > 0;

                        // Quarter condition (if specified)
                        boolean quarterMatches = finalQuarter == null ||
                                finalQuarter.isEmpty() ||
                                finalQuarter.equals(progress.getQuarter());

                        // Subject condition (if specified)
                        boolean subjectMatches = finalSubject == null ||
                                finalSubject.isEmpty() ||
                                finalSubject.equals(progress.getSubject());

                        // ALL conditions must be true
                        return dateRangeMatches && minutesMatch && quarterMatches && subjectMatches;
                    });

            if (hasMatchingProgress) {
                uniqueParticipants.add(userId);
            }
        }

        logger.info("Counted " + uniqueParticipants.size() + " participants for filters - " +
                "quarter: " + quarter + ", subject: " + subject +
                ", dateRange: " + startDate + " to " + endDate +
                ", from " + userIds.size() + " filtered users");

        return uniqueParticipants.size();
    }

    @GetMapping("/completion-rate")
    public ResponseEntity<?> getCompletionRate(
            @RequestParam(required = false) String timeframe,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            logger.info("Getting completion rate with filters: " +
                    "timeframe=" + timeframe +
                    ", gradeLevel=" + gradeLevel +
                    ", section=" + section +
                    ", academicYear=" + academicYear +
                    ", subject=" + subject +
                    ", quarter=" + quarter +
                    ", dateFrom=" + dateFrom +
                    ", dateTo=" + dateTo);

            // Default to monthly if timeframe not specified
            timeframe = (timeframe != null) ? timeframe.toLowerCase() : "monthly";

            // Get current year and week
            LocalDate today = LocalDate.now();
            int currentYear = today.getYear();

            // Parse date filters if provided
            LocalDate fromDate = null;
            LocalDate toDate = null;

            // If academic year is specified (format should be like "2025-2026")
            if (academicYear != null && !academicYear.isEmpty()) {
                try {
                    // Parse the academic year range
                    String[] years = academicYear.split("-");
                    if (years.length == 2) {
                        int startYear = Integer.parseInt(years[0]);
                        int endYear = Integer.parseInt(years[1]);

                        // If no explicit date range is provided, set it based on academic year
                        if (dateFrom == null || dateFrom.isEmpty()) {
                            // Typically academic years start in August/September
                            fromDate = LocalDate.of(startYear, Month.JULY, 1);
                        }

                        if (dateTo == null || dateTo.isEmpty()) {
                            // Typically academic years end in May/June
                            toDate = LocalDate.of(endYear, Month.JUNE, 30);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to parse academic year: " + academicYear);
                    // Continue with default values if parsing fails
                }
            }

            // Explicit date filters override academic year settings
            if (dateFrom != null && !dateFrom.isEmpty()) {
                fromDate = LocalDate.parse(dateFrom, DATE_FORMATTER);
            }

            if (dateTo != null && !dateTo.isEmpty()) {
                toDate = LocalDate.parse(dateTo, DATE_FORMATTER);
            }

            // Apply grade level and section filters to get relevant users
            List<User> filteredUsers = filterUsers(gradeLevel, section);
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

                // Adjust date range if using date filters
                LocalDate startDay = fromDate != null ? fromDate : monday;
                LocalDate endDay = toDate != null ? toDate : monday.plusDays(6);

                // Create a separate map for each day of the week
                Map<DayOfWeek, Map<String, Object>> weekDataMap = new HashMap<>();

                // Initialize all days with zero values
                for (DayOfWeek dow : DayOfWeek.values()) {
                    Map<String, Object> dayData = new LinkedHashMap<>();
                    dayData.put("day", dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                    dayData.put("rate", 0); // Default to zero
                    weekDataMap.put(dow, dayData);
                }

                // Process each day in the date range
                LocalDate currentDate = startDay;
                while (!currentDate.isAfter(endDay)) {
                    DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                    final LocalDate finalCurrentDate = currentDate;

                    // Calculate completion rate for this day using the filtered student IDs
                    double rate = calculateCompletionRate(finalCurrentDate, finalCurrentDate, studentIds, subject,
                            quarter);

                    // Update the map with actual data
                    weekDataMap.get(dayOfWeek).put("rate", Math.round(rate));

                    currentDate = currentDate.plusDays(1);
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
                // For monthly data
                if (fromDate == null && toDate == null) {
                    // If no date filters and no academic year, default to current year
                    fromDate = LocalDate.of(currentYear, 1, 1);
                    toDate = LocalDate.of(currentYear, 12, 31);
                }

                // Ensure we have both dates
                LocalDate startDate = fromDate != null ? fromDate : LocalDate.of(currentYear, 1, 1);
                LocalDate endDate = toDate != null ? toDate : LocalDate.of(currentYear, 12, 31);

                // Create a range of year-month combinations to iterate through
                YearMonth start = YearMonth.from(startDate);
                YearMonth end = YearMonth.from(endDate);

                YearMonth current = start;
                while (!current.isAfter(end)) {
                    int year = current.getYear();
                    int month = current.getMonthValue();

                    // Get start and end of month
                    LocalDate startOfMonth = LocalDate.of(year, month, 1);
                    LocalDate endOfMonth = current.atEndOfMonth();

                    // Create adjusted time bounds that respect the specific date range
                    LocalDate adjustedStartDate = startOfMonth;
                    LocalDate adjustedEndDate = endOfMonth;

                    // If fromDate is provided and is in this month, use it as the start
                    if (fromDate != null && fromDate.getYear() == year && fromDate.getMonthValue() == month) {
                        adjustedStartDate = fromDate;
                    }

                    // If dateTo is provided and is in this month, use it as the end
                    if (toDate != null && toDate.getYear() == year && toDate.getMonthValue() == month) {
                        adjustedEndDate = toDate;
                    }

                    // Create final copies for lambda expressions
                    final LocalDate finalStartDate = adjustedStartDate;
                    final LocalDate finalEndDate = adjustedEndDate;

                    // Calculate completion rate for this month with all filters applied
                    double rate = calculateCompletionRate(finalStartDate, finalEndDate, studentIds, subject, quarter);

                    Map<String, Object> monthData = new LinkedHashMap<>();

                    // Include year in label if spanning multiple years
                    String monthLabel = Month.of(month).toString().substring(0, 3);
                    if (start.getYear() != end.getYear()) {
                        monthLabel += " " + year;
                    }

                    monthData.put("month", monthLabel);
                    monthData.put("rate", Math.round(rate));

                    resultData.add(monthData);

                    // Move to next month
                    current = current.plusMonths(1);
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
            String subject,
            String quarter) {

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

            // Filter with all conditions
            final LocalDate finalStartDate = startDate;
            final LocalDate finalEndDate = endDate;
            final String finalSubject = subject;
            final String finalQuarter = quarter;

            List<LibraryRequirementProgress> filteredProgress = studentProgress.stream()
                    .filter(p -> {
                        // Date range condition
                        boolean dateRangeMatches = p.getLastUpdated() != null &&
                                !p.getLastUpdated().isBefore(finalStartDate) &&
                                !p.getLastUpdated().isAfter(finalEndDate);

                        // Subject condition (if specified)
                        boolean subjectMatches = finalSubject == null ||
                                finalSubject.isEmpty() ||
                                finalSubject.equals(p.getSubject());

                        // Quarter condition (if specified)
                        boolean quarterMatches = finalQuarter == null ||
                                finalQuarter.isEmpty() ||
                                finalQuarter.equals(p.getQuarter());

                        // All conditions must be true
                        return dateRangeMatches && subjectMatches && quarterMatches;
                    })
                    .collect(Collectors.toList());

            progressList.addAll(filteredProgress);
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

    @GetMapping("/completed-requirements")
    public ResponseEntity<?> getCompletedRequirements(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            logger.info("Getting completed requirements with filters: " +
                    "academicYear=" + academicYear +
                    ", gradeLevel=" + gradeLevel +
                    ", section=" + section +
                    ", subject=" + subject +
                    ", quarter=" + quarter +
                    ", dateFrom=" + dateFrom +
                    ", dateTo=" + dateTo);

            // Get current year
            int currentYear = LocalDate.now().getYear();

            // Parse date filters if provided
            LocalDate fromDate = null;
            LocalDate toDate = null;

            // If academic year is specified (format should be like "2025-2026")
            if (academicYear != null && !academicYear.isEmpty()) {
                try {
                    // Parse the academic year range
                    String[] years = academicYear.split("-");
                    if (years.length == 2) {
                        int startYear = Integer.parseInt(years[0]);
                        int endYear = Integer.parseInt(years[1]);

                        // If no explicit date range is provided, set it based on academic year
                        if (dateFrom == null || dateFrom.isEmpty()) {
                            // Typically academic years start in August/September
                            fromDate = LocalDate.of(startYear, Month.JULY, 1);
                        }

                        if (dateTo == null || dateTo.isEmpty()) {
                            // Typically academic years end in May/June
                            toDate = LocalDate.of(endYear, Month.JUNE, 30);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to parse academic year: " + academicYear);
                    // Continue with default values if parsing fails
                }
            }

            // Explicit date filters override academic year settings
            if (dateFrom != null && !dateFrom.isEmpty()) {
                fromDate = LocalDate.parse(dateFrom, DATE_FORMATTER);
            }

            if (dateTo != null && !dateTo.isEmpty()) {
                toDate = LocalDate.parse(dateTo, DATE_FORMATTER);
            }

            // Create response data for each month
            List<Map<String, Object>> monthlyData = new ArrayList<>();

            // If no date filters, default to current year
            if (fromDate == null && toDate == null) {
                fromDate = LocalDate.of(currentYear, 1, 1);
                toDate = LocalDate.of(currentYear, 12, 31);
            }

            // Create a range of year-month combinations to iterate through
            YearMonth start = YearMonth.from(fromDate);
            YearMonth end = YearMonth.from(toDate);

            YearMonth current = start;
            while (!current.isAfter(end)) {
                int year = current.getYear();
                int month = current.getMonthValue();

                // Get start and end of month
                LocalDate startOfMonth = LocalDate.of(year, month, 1);
                LocalDate endOfMonth = current.atEndOfMonth();

                // Create adjusted time bounds that respect the specific date range
                LocalDate adjustedStartDate = startOfMonth;
                LocalDate adjustedEndDate = endOfMonth;

                // If fromDate is provided and is in this month, use it as the start
                if (fromDate != null && fromDate.getYear() == year && fromDate.getMonthValue() == month) {
                    adjustedStartDate = fromDate;
                }

                // If dateTo is provided and is in this month, use it as the end
                if (toDate != null && toDate.getYear() == year && toDate.getMonthValue() == month) {
                    adjustedEndDate = toDate;
                }

                // Create final copies for lambda expressions
                final LocalDate finalStartDate = adjustedStartDate;
                final LocalDate finalEndDate = adjustedEndDate;
                final String finalSubject = (subject != null && !subject.isEmpty()) ? subject : null;
                final String finalQuarter = (quarter != null && !quarter.isEmpty()) ? quarter : null;

                // Apply grade and section filters to get relevant users
                List<User> filteredUsers = filterUsers(gradeLevel, section);
                List<String> studentIds = filteredUsers.stream()
                        .map(User::getIdNumber)
                        .collect(Collectors.toList());

                // Manual filtering approach
                List<LibraryRequirementProgress> allProgress = new ArrayList<>();

                // Get progress for each filtered student
                for (String studentId : studentIds) {
                    List<LibraryRequirementProgress> studentProgress = progressRepository
                            .findByStudentId(studentId);
                    allProgress.addAll(studentProgress);
                }

                // Filter by all criteria together with proper AND logic
                long completedRequirements = allProgress.stream()
                        .filter(p -> {
                            // Completion status
                            boolean isCompleted = p.getIsCompleted();

                            // Date range condition
                            boolean dateRangeMatches = p.getLastUpdated() != null &&
                                    !p.getLastUpdated().isBefore(finalStartDate) &&
                                    !p.getLastUpdated().isAfter(finalEndDate);

                            // Subject condition (if specified)
                            boolean subjectMatches = finalSubject == null ||
                                    finalSubject.equals(p.getSubject());

                            // Quarter condition (if specified)
                            boolean quarterMatches = finalQuarter == null ||
                                    finalQuarter.equals(p.getQuarter());

                            // All conditions must be true
                            return isCompleted && dateRangeMatches && subjectMatches && quarterMatches;
                        })
                        .count();

                Map<String, Object> monthData = new LinkedHashMap<>();

                // Include year in label if spanning multiple years
                String monthLabel = Month.of(month).toString().substring(0, 3);
                if (start.getYear() != end.getYear() || academicYear != null) {
                    monthLabel += " " + year;
                }

                monthData.put("month", monthLabel);
                monthData.put("count", completedRequirements);

                monthlyData.add(monthData);

                // Move to next month
                current = current.plusMonths(1);
            }

            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch completed requirements statistics: " + e.getMessage()));
        }
    }

    /**
     * Enhanced filterUsers method that properly handles filters
     * Updated to NOT use academicYear filter for students
     */
    public List<User> filterUsers(String gradeLevel, String section) {
        List<User> allUsers = userRepository.findByRole("Student");

        // Log the filter parameters for debugging
        logger.info("Filtering users with criteria: gradeLevel=" + gradeLevel +
                ", section=" + section);

        // Create a filtered list with grade and section filters applied
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // Grade level filter (handle different formats)
                    boolean gradeMatches = gradeLevel == null || gradeLevel.isEmpty() ||
                            "All Grades".equals(gradeLevel) ||
                            gradeLevel.equals(user.getGrade()) ||
                            gradeLevel.equals("Grade " + user.getGrade());

                    // Section filter
                    boolean sectionMatches = section == null || section.isEmpty() ||
                            section.equals(user.getSection());

                    // Only apply grade and section filters - academicYear is used only for date
                    // ranges
                    return gradeMatches && sectionMatches;
                })
                .collect(Collectors.toList());

        logger.info("Filtered " + allUsers.size() + " users down to " + filteredUsers.size() +
                " based on gradeLevel and section");

        return filteredUsers;
    }
}