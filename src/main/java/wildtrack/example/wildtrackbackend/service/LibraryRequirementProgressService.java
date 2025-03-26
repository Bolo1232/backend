package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.SetLibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@Service
public class LibraryRequirementProgressService {
    private static final Logger logger = Logger.getLogger(LibraryRequirementProgressService.class.getName());

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    @Autowired
    private SetLibraryHoursRepository requirementRepository;

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Initialize only new requirements for a student
     * This should only fetch requirements created after the user's registration
     */
    @Transactional
    public void initializeRequirements(String studentId) {
        logger.info("Initializing requirements for student: " + studentId);

        // Get the student
        Optional<User> userOpt = userRepository.findByIdNumber(studentId);
        if (!userOpt.isPresent()) {
            logger.warning("Student not found: " + studentId);
            return;
        }

        User student = userOpt.get();
        String gradeLevel = student.getGrade();

        // Get user's registration date/creation date - with fix for effectively final
        // variable
        final LocalDateTime userCreationDate = student.getCreatedAt() != null
                ? student.getCreatedAt()
                : LocalDateTime.now().minusYears(1);

        if (student.getCreatedAt() == null) {
            logger.warning("User has no creation date, using fallback date: " + userCreationDate);
        }

        // Get all APPROVED requirements for this grade level
        List<SetLibraryHours> allRequirements = requirementRepository.findByGradeLevelAndApprovalStatus(gradeLevel,
                "APPROVED");

        // Filter requirements to only include those created after user registration
        List<SetLibraryHours> newRequirements = allRequirements.stream()
                .filter(req -> req.getCreatedAt() != null && req.getCreatedAt().isAfter(userCreationDate))
                .collect(Collectors.toList());

        logger.info("Found " + newRequirements.size() + " new requirements for student " + studentId +
                " out of " + allRequirements.size() + " total requirements");

        // For each new requirement, create a progress record if it doesn't exist
        for (SetLibraryHours requirement : newRequirements) {
            // Check if progress record already exists
            Optional<LibraryRequirementProgress> existingProgress = progressRepository
                    .findByStudentIdAndRequirementId(studentId, requirement.getId());

            if (!existingProgress.isPresent()) {
                // Create new progress record
                LibraryRequirementProgress progress = new LibraryRequirementProgress(
                        studentId,
                        requirement.getId(),
                        requirement.getSubject(),
                        requirement.getQuarter().getValue(),
                        gradeLevel,
                        requirement.getMinutes(),
                        requirement.getDeadline());

                progressRepository.save(progress);
                logger.info("Created progress record for student " + studentId +
                        " and requirement " + requirement.getId());
            }
        }
    }

    /**
     * Check for new requirements for a student
     * This should be called periodically to capture new requirements added by
     * teachers
     */
    @Transactional
    public List<LibraryRequirementProgress> checkForNewRequirements(String studentId) {
        logger.info("Checking for new requirements for student: " + studentId);

        // Get existing requirements
        List<LibraryRequirementProgress> existingProgress = progressRepository.findByStudentId(studentId);

        // Collect IDs of requirements already assigned to this student
        Set<Long> existingRequirementIds = existingProgress.stream()
                .map(LibraryRequirementProgress::getRequirementId)
                .collect(Collectors.toSet());

        // Initialize new requirements (this will only create ones that don't exist yet)
        initializeRequirements(studentId);

        // Fetch updated list of requirements (including any newly added ones)
        List<LibraryRequirementProgress> updatedProgress = progressRepository.findByStudentId(studentId);

        // Find newly added requirements
        List<LibraryRequirementProgress> newlyAddedRequirements = updatedProgress.stream()
                .filter(req -> !existingRequirementIds.contains(req.getRequirementId()))
                .collect(Collectors.toList());

        if (!newlyAddedRequirements.isEmpty()) {
            logger.info("Found " + newlyAddedRequirements.size() + " new requirements for student " + studentId);

            // Create a notification for the student about new requirements
            if (newlyAddedRequirements.size() > 0) {
                Optional<User> userOpt = userRepository.findByIdNumber(studentId);
                if (userOpt.isPresent()) {
                    String title = "New Reading Requirements";
                    String message = String.format(
                            "You have %d new reading requirement(s) assigned to you. Please check your requirements page for details.",
                            newlyAddedRequirements.size());

                    notificationService.createUserNotification(
                            userOpt.get().getId(),
                            title,
                            message,
                            "NEW_REQUIREMENTS",
                            null);
                }
            }
        }

        // Return all requirements including any new ones
        return updatedProgress;
    }

    /**
     * Record library time for a student
     * This should be called when a student times out
     */
    @Transactional
    public void recordLibraryTime(Long libraryHoursId) {
        logger.info("Recording library time for library hours ID: " + libraryHoursId);

        // Get the library hours record
        Optional<LibraryHours> libraryHoursOpt = libraryHoursRepository.findById(libraryHoursId);
        if (!libraryHoursOpt.isPresent() || libraryHoursOpt.get().getTimeOut() == null) {
            logger.warning("Invalid library hours record: " + libraryHoursId);
            return;
        }

        LibraryHours hours = libraryHoursOpt.get();
        String studentId = hours.getIdNumber();
        String subject = hours.getSubject();

        // Calculate minutes
        int minutes = calculateMinutes(hours.getTimeIn(), hours.getTimeOut());
        if (minutes <= 0) {
            logger.warning("No valid minutes to record for library hours ID: " + libraryHoursId);
            return;
        }

        // Check if student has any requirements, initialize if needed
        long requirementCount = progressRepository.countByStudentId(studentId);
        if (requirementCount == 0) {
            logger.info("Initializing requirements for student before recording time: " + studentId);
            initializeRequirements(studentId);
        } else {
            // Also check for new requirements in case all existing ones are completed
            checkForNewRequirements(studentId);
        }

        // Find matching requirement progress
        Optional<LibraryRequirementProgress> progressOpt;

        if (subject != null && !subject.isEmpty()) {
            // Find all requirements for this subject
            List<LibraryRequirementProgress> subjectRequirements = progressRepository
                    .findByStudentIdAndSubject(studentId, subject);

            // Sort by quarter (First, Second, Third, Fourth)
            subjectRequirements.sort((a, b) -> {
                int quarterA = getQuarterValue(a.getQuarter());
                int quarterB = getQuarterValue(b.getQuarter());
                return Integer.compare(quarterA, quarterB);
            });

            // Find first incomplete requirement for this subject
            progressOpt = subjectRequirements.stream()
                    .filter(progress -> !progress.getIsCompleted())
                    .findFirst();
        } else {
            // Get all progress records for the student
            List<LibraryRequirementProgress> allProgress = progressRepository.findByStudentId(studentId);

            // Sort by quarter (First, Second, Third, Fourth)
            allProgress.sort((a, b) -> {
                // Convert quarter names to numerical values for sorting
                int quarterA = getQuarterValue(a.getQuarter());
                int quarterB = getQuarterValue(b.getQuarter());
                return Integer.compare(quarterA, quarterB);
            });

            // Find the first incomplete requirement
            progressOpt = allProgress.stream()
                    .filter(progress -> !progress.getIsCompleted())
                    .findFirst();
        }

        if (progressOpt.isPresent()) {
            LibraryRequirementProgress progress = progressOpt.get();
            boolean wasCompleted = progress.getIsCompleted();

            // Add minutes
            progress.addMinutes(minutes);

            // Save the updated progress
            progressRepository.save(progress);
            logger.info("Added " + minutes + " minutes to progress ID: " + progress.getId() +
                    " (" + progress.getSubject() + " - " + progress.getQuarter() + " Quarter)");

            // Check if the requirement was just completed
            if (!wasCompleted && progress.getIsCompleted()) {
                // Send completion notification
                sendCompletionNotification(studentId, progress);
            }
        } else {
            logger.warning("No incomplete requirement progress found for student " + studentId +
                    (subject != null ? " for subject " + subject : ""));
        }
    }

    /**
     * Get all requirement progress for a student without auto-initialization
     */
    public List<LibraryRequirementProgress> getStudentProgress(String studentId) {
        // Simply return existing progress - no auto-initialization
        return progressRepository.findByStudentId(studentId);
    }

    /**
     * Get progress summary for a student without auto-initialization
     */
    public Map<String, Object> getProgressSummary(String studentId) {
        Map<String, Object> summary = new HashMap<>();

        // Skip auto-initialization - just use existing records

        // Get counts
        Integer totalRequirements = (int) progressRepository.countByStudentId(studentId);
        Integer completedRequirements = progressRepository.countRequirementsByStatus(studentId, true);
        Integer inProgressRequirements = progressRepository.countRequirementsByStatus(studentId, false);
        Integer overdueRequirements = progressRepository.countOverdueRequirements(studentId);

        // Get totals
        Integer totalMinutesRequired = progressRepository.getTotalRequiredMinutes(studentId);
        Integer totalMinutesRendered = progressRepository.getTotalMinutesRendered(studentId);

        // Calculate overall percentage
        double overallPercentage = 0;
        if (totalMinutesRequired != null && totalMinutesRequired > 0) {
            overallPercentage = (double) totalMinutesRendered / totalMinutesRequired * 100;
        }

        // Build summary
        summary.put("totalRequirements", totalRequirements);
        summary.put("completedRequirements", completedRequirements);
        summary.put("inProgressRequirements", inProgressRequirements);
        summary.put("overdueRequirements", overdueRequirements);
        summary.put("totalMinutesRequired", totalMinutesRequired);
        summary.put("totalMinutesRendered", totalMinutesRendered);
        summary.put("overallPercentage", Math.min(100.0, overallPercentage));

        return summary;
    }

    /**
     * Get progress summary for a student with auto-initialization
     * This will initialize requirements if none exist and check for new ones
     */
    public Map<String, Object> getProgressSummaryWithInit(String studentId) {
        // Check if student has any requirements
        long requirementCount = progressRepository.countByStudentId(studentId);

        if (requirementCount == 0) {
            // Auto-initialize if no requirements found
            logger.info("Auto-initializing requirements for student: " + studentId);
            initializeRequirements(studentId);
        } else {
            // Check for new requirements if student already has some
            checkForNewRequirements(studentId);
        }

        // Return summary
        return getProgressSummary(studentId);
    }

    /**
     * Helper method to convert quarter names to numerical values for sorting
     */
    private int getQuarterValue(String quarter) {
        switch (quarter) {
            case "First":
                return 1;
            case "Second":
                return 2;
            case "Third":
                return 3;
            case "Fourth":
                return 4;
            default:
                return 5; // For unknown quarters
        }
    }

    /**
     * Send notification for completed requirement
     */
    private void sendCompletionNotification(String studentId, LibraryRequirementProgress progress) {
        // Get the student
        Optional<User> userOpt = userRepository.findByIdNumber(studentId);
        if (!userOpt.isPresent()) {
            return;
        }

        User student = userOpt.get();

        // Create notification message
        String title = "Reading Requirement Completed!";
        String message = String.format(
                "Congratulations! You have completed the %s reading requirement for %s Quarter. " +
                        "You successfully completed %d minutes of reading.",
                progress.getSubject(),
                progress.getQuarter(),
                progress.getRequiredMinutes());

        // Send notification
        notificationService.createUserNotification(
                student.getId(),
                title,
                message,
                "REQUIREMENT_COMPLETED",
                progress.getRequirementId());
    }

    /**
     * Calculate minutes between time in and time out
     */
    private int calculateMinutes(LocalDateTime timeIn, LocalDateTime timeOut) {
        if (timeIn == null || timeOut == null) {
            return 0;
        }
        long minutes = ChronoUnit.MINUTES.between(timeIn, timeOut);
        return (int) Math.max(0, minutes);
    }
}