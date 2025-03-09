package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.ActivityLog;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.ActivityLogRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    // Default academic year - in a real app, this would come from a configuration
    private static final String DEFAULT_ACADEMIC_YEAR = "2023-2024";

    /**
     * Log an activity for a user
     */
    public ActivityLog logActivity(Long userId, String activity, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        String formattedName = formatUserName(user);

        ActivityLog log = new ActivityLog(
                userId,
                formattedName,
                user.getRole(),
                activity,
                description,
                DEFAULT_ACADEMIC_YEAR);

        return activityLogRepository.save(log);
    }

    /**
     * Format user's full name
     */
    private String formatUserName(User user) {
        if (user.getMiddleName() != null && !user.getMiddleName().trim().isEmpty()) {
            return String.format("%s %s. %s",
                    user.getFirstName(),
                    user.getMiddleName().charAt(0),
                    user.getLastName());
        } else {
            return String.format("%s %s", user.getFirstName(), user.getLastName());
        }
    }

    /**
     * Get all activity logs
     */
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogRepository.findAll();
    }

    /**
     * Get activity logs with filters
     */
    public List<ActivityLog> getActivityLogs(String academicYear,
            LocalDate dateFrom,
            LocalDate dateTo,
            String searchQuery) {
        // Convert dates to LocalDateTime for the repository
        LocalDateTime startDateTime = dateFrom != null ? LocalDateTime.of(dateFrom, LocalTime.MIN) : null;

        LocalDateTime endDateTime = dateTo != null ? LocalDateTime.of(dateTo, LocalTime.MAX) : null;

        // If search query is empty, set it to null for the query
        String keyword = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;

        return activityLogRepository.findByFilters(
                academicYear,
                startDateTime,
                endDateTime,
                keyword);
    }

    /**
     * Log when a teacher creates library hours requirement
     */
    public void logLibraryHoursCreation(Long userId, String subject, String gradeLevel, String quarter) {
        String description = String.format(
                "Created library hours requirement for %s %s, %s Quarter",
                subject, gradeLevel, quarter);

        logActivity(userId, "LIBRARY_HOURS_CREATED", description);
    }

    /**
     * Log when a librarian approves library hours requirement
     */
    public void logLibraryHoursApproval(Long librarianId, Long requirementId,
            String subject, String gradeLevel, String quarter) {
        String description = String.format(
                "Approved library hours requirement ID #%d for %s %s, %s Quarter",
                requirementId, subject, gradeLevel, quarter);

        logActivity(librarianId, "LIBRARY_HOURS_APPROVED", description);
    }

    /**
     * Log when a librarian rejects library hours requirement
     */
    public void logLibraryHoursRejection(Long librarianId, Long requirementId,
            String subject, String gradeLevel, String quarter,
            String reason) {
        String description = String.format(
                "Rejected library hours requirement ID #%d for %s %s, %s Quarter. Reason: %s",
                requirementId, subject, gradeLevel, quarter, reason);

        logActivity(librarianId, "LIBRARY_HOURS_REJECTED", description);
    }
}