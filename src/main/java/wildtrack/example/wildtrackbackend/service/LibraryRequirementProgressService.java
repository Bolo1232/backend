package wildtrack.example.wildtrackbackend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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
     * Initialize requirements for a student based on when they joined their current
     * grade
     * 
     * This will ONLY fetch requirements created AFTER the student's grade update
     * date
     * or registration date if the student has always been in this grade level
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

        // Get the effective date for when the student joined this grade level
        LocalDateTime gradeJoinDate = getGradeJoinDate(student);
        logger.info("Using grade join date: " + gradeJoinDate + " for student: " + studentId);

        // Get all APPROVED requirements for this grade level
        List<SetLibraryHours> allRequirements = requirementRepository.findByGradeLevel(gradeLevel);

        // IMPORTANT: Filter requirements to ONLY include those created AFTER the
        // student joined this grade level
        List<SetLibraryHours> newRequirements = allRequirements.stream()
                .filter(req -> req.getCreatedAt() != null && req.getCreatedAt().isAfter(gradeJoinDate))
                .collect(Collectors.toList());

        logger.info("Found " + newRequirements.size() + " new requirements for student " + studentId +
                " out of " + allRequirements.size() + " total requirements for grade " + gradeLevel);

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
     * Get the date when student joined their current grade level.
     * This accounts for grade updates or initial enrollment.
     */
    private LocalDateTime getGradeJoinDate(User student) {
        // Check if the student has a grade update timestamp
        if (student.getGradeUpdatedAt() != null) {
            return student.getGradeUpdatedAt();
        }

        // Fall back to creation date if no grade update timestamp exists
        if (student.getCreatedAt() != null) {
            return student.getCreatedAt();
        }

        // Last resort fallback if no dates are available
        LocalDateTime fallbackDate = LocalDateTime.now().minusYears(1);
        logger.warning("No grade update or creation date found for student ID " +
                student.getIdNumber() + ", using fallback date: " + fallbackDate);
        return fallbackDate;
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
     * Get active progress with timing status
     * This method returns progress with real-time status information based on
     * current library hours
     */
    public List<Map<String, Object>> getActiveProgressWithTimingStatus(String studentId) {
        // Get all the student's requirements
        List<LibraryRequirementProgress> progress = progressRepository.findByStudentId(studentId);

        // Check if student is currently timed in
        Optional<LibraryHours> activeSession = libraryHoursRepository.findLatestByIdNumber(studentId)
                .filter(lh -> lh.getTimeOut() == null); // No timeout means active session

        // MODIFIED: Sort requirements by deadline (earliest first) instead of quarter
        progress.sort((a, b) -> {
            // Handle null deadlines - put them at the end
            if (a.getDeadline() == null && b.getDeadline() == null) {
                return 0;
            } else if (a.getDeadline() == null) {
                return 1;
            } else if (b.getDeadline() == null) {
                return -1;
            }
            // Otherwise sort by deadline (earliest first)
            return a.getDeadline().compareTo(b.getDeadline());
        });

        // Get the first incomplete requirement by deadline priority
        // This will be our primary requirement based on priority
        Optional<LibraryRequirementProgress> highestPriorityIncomplete = progress.stream()
                .filter(req -> !req.getIsCompleted())
                .findFirst();

        // Set the active requirement ID to the highest priority one
        Long activeRequirementId = null;
        if (highestPriorityIncomplete.isPresent()) {
            activeRequirementId = highestPriorityIncomplete.get().getId();
        }

        // If a student is currently timed in and has a specific subject
        boolean userIsActive = false;
        final String activeSubject; // Declare as final for use in lambda expression

        if (activeSession.isPresent() && activeSession.get().getSubject() != null
                && !activeSession.get().getSubject().isEmpty()) {
            userIsActive = true;
            activeSubject = activeSession.get().getSubject(); // Assign once, make it effectively final

            // If they have a specific subject selected, find matching requirements
            // MODIFIED: Get all incomplete requirements for this subject, sorted by
            // deadline
            List<LibraryRequirementProgress> matchingSubjectReqs = progress.stream()
                    .filter(req -> !req.getIsCompleted() &&
                            activeSubject.equals(req.getSubject()))
                    .sorted((a, b) -> {
                        // Sort by deadline (earliest first)
                        if (a.getDeadline() == null && b.getDeadline() == null) {
                            return 0;
                        } else if (a.getDeadline() == null) {
                            return 1;
                        } else if (b.getDeadline() == null) {
                            return -1;
                        }
                        return a.getDeadline().compareTo(b.getDeadline());
                    })
                    .toList(); // Using toList() instead of collect(Collectors.toList())

            // If there are matching requirements for this subject, use the highest priority
            // one (earliest deadline)
            if (!matchingSubjectReqs.isEmpty()) {
                activeRequirementId = matchingSubjectReqs.get(0).getId();
            }
        } else {
            activeSubject = null; // Initialize with null if no active session
        }

        // Convert to response format with additional status information
        List<Map<String, Object>> result = new ArrayList<>();

        for (LibraryRequirementProgress req : progress) {
            Map<String, Object> item = new HashMap<>();

            // Copy all properties
            item.put("id", req.getId());
            item.put("requirementId", req.getRequirementId());
            item.put("studentId", req.getStudentId());
            item.put("subject", req.getSubject());
            item.put("quarter", req.getQuarter());
            item.put("gradeLevel", req.getGradeLevel());
            item.put("requiredMinutes", req.getRequiredMinutes());
            item.put("minutesRendered", req.getMinutesRendered());
            item.put("remainingMinutes", req.getRemainingMinutes());
            item.put("deadline", req.getDeadline());
            item.put("isCompleted", req.getIsCompleted());
            item.put("progressPercentage", req.getProgressPercentage());

            // Fetch the original requirement to get task and creator information
            Optional<SetLibraryHours> libraryHoursOpt = requirementRepository.findById(req.getRequirementId());
            if (libraryHoursOpt.isPresent()) {
                SetLibraryHours libraryHours = libraryHoursOpt.get();

                // Set the task field
                item.put("task", libraryHours.getTask());

                // Get creator information if available
                if (libraryHours.getCreatedById() != null) {
                    Optional<User> creatorOpt = userRepository.findById(libraryHours.getCreatedById());
                    if (creatorOpt.isPresent()) {
                        User creator = creatorOpt.get();
                        item.put("creatorName", creator.getFirstName() + " " + creator.getLastName());
                    } else {
                        item.put("creatorName", "Unknown Teacher");
                    }
                } else {
                    item.put("creatorName", "Unknown Teacher");
                }
            } else {
                item.put("task", null);
                item.put("creatorName", "Unknown Teacher");
            }

            // IMPROVED STATUS LOGIC:
            String status;

            if (req.getIsCompleted()) {
                // If requirement is completed, it's always "Completed"
                status = "Completed";
            } else if (userIsActive && activeRequirementId != null && req.getId().equals(activeRequirementId)) {
                // If user is actively timed in and this is the active requirement, it's "In
                // Progress"
                // even if it has 0 minutes rendered
                status = "In Progress";
            } else if (req.getMinutesRendered() <= 0) {
                // If not active and no minutes rendered, it's "Not Started"
                status = "Not Started";
            } else if (activeRequirementId != null && req.getId().equals(activeRequirementId)) {
                // If this is the highest priority requirement with minutes but user isn't timed
                // in
                status = req.getDeadline() != null && req.getDeadline().isBefore(LocalDate.now())
                        ? "Overdue"
                        : "In Progress";
            } else if (req.getDeadline() != null && req.getDeadline().isBefore(LocalDate.now())) {
                // Lower priority requirement with a past deadline
                status = "Overdue";
            } else if (req.getMinutesRendered() > 0) {
                // If this requirement has minutes but isn't active/highest priority
                status = "Paused";
            } else {
                // Fallback to entity status
                status = req.getStatus();
            }

            item.put("status", status);

            // Add count of contributing sessions
            List<Long> contributingSessions = req.getContributingLibraryHoursIdsList();
            item.put("contributingSessionsCount", contributingSessions.size());

            result.add(item);
        }

        return result;
    }

    /**
     * Helper method to get current quarter based on date
     */
    private String getCurrentQuarter() {
        int month = LocalDate.now().getMonthValue();

        if (month >= 8 && month <= 10) {
            return "First";
        } else if (month >= 11 || month <= 1) {
            return "Second";
        } else if (month >= 2 && month <= 4) {
            return "Third";
        } else {
            return "Fourth";
        }
    }

    /**
     * Record library time for a student
     * This should be called when a student times out
     */
    @Transactional
    public void recordLibraryTime(Long libraryHoursId) {
        logger.info("Recording library time for library hours ID: " + libraryHoursId);

        // Input validation
        if (libraryHoursId == null) {
            logger.warning("NULL library hours ID provided");
            return;
        }

        // Get the library hours record
        Optional<LibraryHours> libraryHoursOpt = libraryHoursRepository.findById(libraryHoursId);
        if (!libraryHoursOpt.isPresent() || libraryHoursOpt.get().getTimeOut() == null) {
            logger.warning("Invalid library hours record: " + libraryHoursId);
            return;
        }

        LibraryHours hours = libraryHoursOpt.get();

        // Check if this session has already been processed
        if (hours.getRequirementId() != null && hours.getIsCounted()) {
            logger.info("Library hours already counted for a requirement: " + libraryHoursId);
            return;
        }

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

            // MODIFIED: Sort by deadline (earliest first) instead of quarter
            subjectRequirements.sort((a, b) -> {
                // Handle null deadlines - put them at the end
                if (a.getDeadline() == null && b.getDeadline() == null) {
                    return 0;
                } else if (a.getDeadline() == null) {
                    return 1;
                } else if (b.getDeadline() == null) {
                    return -1;
                }
                // Otherwise sort by deadline (earliest first)
                return a.getDeadline().compareTo(b.getDeadline());
            });

            // Always choose the earliest deadline that isn't completed
            progressOpt = subjectRequirements.stream()
                    .filter(progress -> !progress.getIsCompleted())
                    .findFirst();
        } else {
            // Get all progress records for the student
            List<LibraryRequirementProgress> allProgress = progressRepository.findByStudentId(studentId);

            // MODIFIED: Sort by deadline (earliest first) instead of quarter
            allProgress.sort((a, b) -> {
                // Handle null deadlines - put them at the end
                if (a.getDeadline() == null && b.getDeadline() == null) {
                    return 0;
                } else if (a.getDeadline() == null) {
                    return 1;
                } else if (b.getDeadline() == null) {
                    return -1;
                }
                // Otherwise sort by deadline (earliest first)
                return a.getDeadline().compareTo(b.getDeadline());
            });

            // Find the first incomplete requirement by deadline priority
            progressOpt = allProgress.stream()
                    .filter(progress -> !progress.getIsCompleted())
                    .findFirst();
        }

        if (progressOpt.isPresent()) {
            LibraryRequirementProgress progress = progressOpt.get();
            boolean wasCompleted = progress.getIsCompleted();

            // Add minutes
            progress.addMinutes(minutes);

            // Add the library hours ID to the contributing sessions
            progress.addContributingLibraryHoursId(libraryHoursId);

            // Update the library hours record to point to this requirement
            hours.setRequirementId(progress.getId());
            libraryHoursRepository.save(hours);

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
     * Handle a student's grade change
     * Updates grade update timestamp and initializes new requirements for current
     * grade
     */
    @Transactional
    public void handleGradeChange(String studentId, String newGrade) {
        logger.info("Handling grade change for student: " + studentId + " to grade: " + newGrade);

        // Get the student
        Optional<User> userOpt = userRepository.findByIdNumber(studentId);
        if (!userOpt.isPresent()) {
            logger.warning("Student not found for grade change: " + studentId);
            return;
        }

        User student = userOpt.get();

        // Update grade and grade update timestamp
        String oldGrade = student.getGrade();
        student.setGrade(newGrade);

        // Set grade update timestamp
        student.setGradeUpdatedAt(LocalDateTime.now());
        userRepository.save(student);

        logger.info("Updated grade for student " + studentId + " from " + oldGrade +
                " to " + newGrade + " with timestamp " + student.getGradeUpdatedAt());

        // Now that grade is updated with timestamp, initialize any new requirements
        // This will use the grade update timestamp to only get requirements created
        // after
        initializeRequirements(studentId);
    }

    /**
     * Get detailed information about a requirement including contributing library
     * hours sessions
     */
    public Map<String, Object> getRequirementDetailsWithContributingSessions(Long requirementId, Integer page,
            Integer size) {
        Optional<LibraryRequirementProgress> progressOpt = progressRepository.findById(requirementId);

        if (!progressOpt.isPresent()) {
            throw new RuntimeException("Requirement not found with ID: " + requirementId);
        }

        LibraryRequirementProgress progress = progressOpt.get();
        Map<String, Object> details = new HashMap<>();

        // Add basic requirement details
        details.put("id", progress.getId());
        details.put("studentId", progress.getStudentId());
        details.put("subject", progress.getSubject());
        details.put("quarter", progress.getQuarter());
        details.put("gradeLevel", progress.getGradeLevel());
        details.put("requiredMinutes", progress.getRequiredMinutes());
        details.put("minutesRendered", progress.getMinutesRendered());
        details.put("remainingMinutes", progress.getRemainingMinutes());
        details.put("deadline", progress.getDeadline());
        details.put("isCompleted", progress.getIsCompleted());
        details.put("progressPercentage", progress.getProgressPercentage());
        details.put("status", progress.getStatus());

        // Get task information
        Optional<SetLibraryHours> libraryHoursOpt = requirementRepository.findById(progress.getRequirementId());
        if (libraryHoursOpt.isPresent()) {
            SetLibraryHours requirement = libraryHoursOpt.get();
            details.put("task", requirement.getTask());

            // Get creator information if available
            if (requirement.getCreatedById() != null) {
                Optional<User> creatorOpt = userRepository.findById(requirement.getCreatedById());
                if (creatorOpt.isPresent()) {
                    User creator = creatorOpt.get();
                    details.put("creatorName", creator.getFirstName() + " " + creator.getLastName());
                } else {
                    details.put("creatorName", "Unknown Teacher");
                }
            } else {
                details.put("creatorName", "Unknown Teacher");
            }
        } else {
            details.put("task", null);
            details.put("creatorName", "Unknown Teacher");
        }

        // Get contributing library hours sessions with pagination
        List<Long> allContributingSessionIds = progress.getContributingLibraryHoursIdsList();

        // Calculate pagination values
        int pageNum = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10; // Default page size

        // Calculate total pages
        int totalItems = allContributingSessionIds.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Get subset of IDs for the requested page
        List<Long> paginatedIds;
        int fromIndex = pageNum * pageSize;
        if (fromIndex >= allContributingSessionIds.size()) {
            paginatedIds = new ArrayList<>();
        } else {
            int toIndex = Math.min(fromIndex + pageSize, allContributingSessionIds.size());
            paginatedIds = allContributingSessionIds.subList(fromIndex, toIndex);
        }

        List<Map<String, Object>> contributingSessions = new ArrayList<>();

        // Only process the IDs for the current page
        for (Long sessionId : paginatedIds) {
            Optional<LibraryHours> sessionOpt = libraryHoursRepository.findById(sessionId);
            if (sessionOpt.isPresent()) {
                LibraryHours session = sessionOpt.get();
                Map<String, Object> sessionDetails = new HashMap<>();

                sessionDetails.put("id", session.getId());
                sessionDetails.put("timeIn", session.getTimeIn());
                sessionDetails.put("timeOut", session.getTimeOut());
                sessionDetails.put("bookTitle", session.getBookTitle());
                sessionDetails.put("summary", session.getSummary());

                // Calculate minutes for this session
                int sessionMinutes = 0;
                if (session.getTimeIn() != null && session.getTimeOut() != null) {
                    sessionMinutes = (int) Duration.between(session.getTimeIn(), session.getTimeOut()).toMinutes();
                }
                sessionDetails.put("minutes", sessionMinutes);

                // Calculate contribution percentage
                double contributionPercentage = 0;
                if (progress.getRequiredMinutes() > 0) {
                    contributionPercentage = (double) sessionMinutes / progress.getRequiredMinutes() * 100;
                }
                sessionDetails.put("contributionPercentage", Math.min(100.0, contributionPercentage));

                contributingSessions.add(sessionDetails);
            }
        }

        // Sort sessions by date (newest first)
        contributingSessions.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timeIn");
            LocalDateTime timeB = (LocalDateTime) b.get("timeIn");
            return timeB.compareTo(timeA);
        });

        details.put("contributingSessions", contributingSessions);
        details.put("pagination", Map.of(
                "totalItems", totalItems,
                "totalPages", totalPages,
                "currentPage", pageNum,
                "pageSize", pageSize));

        return details;
    }

    /**
     * Migrate existing library hours data to link with requirements
     * This method processes all library hours records that have a requirement ID
     * assigned
     * and adds them to the contributing sessions list of the corresponding
     * requirement
     */
    @Transactional
    public void migrateExistingLibraryHoursToRequirements() {
        logger.info("Starting migration of existing library hours to requirement contributing sessions");

        // Get all library hours with a requirementId set
        List<LibraryHours> libraryHoursWithRequirement = libraryHoursRepository.findByRequirementIdIsNotNull();

        int processed = 0;
        int errors = 0;

        for (LibraryHours hours : libraryHoursWithRequirement) {
            try {
                // Find the requirement
                Optional<LibraryRequirementProgress> progressOpt = progressRepository
                        .findById(hours.getRequirementId());

                if (progressOpt.isPresent()) {
                    LibraryRequirementProgress progress = progressOpt.get();

                    // Add the library hours ID to the contributing sessions
                    progress.addContributingLibraryHoursId(hours.getId());

                    // Save the updated progress
                    progressRepository.save(progress);
                    processed++;
                } else {
                    logger.warning("Referenced requirement not found: " + hours.getRequirementId());
                    errors++;
                }
            } catch (Exception e) {
                logger.severe("Error processing library hours " + hours.getId() + ": " + e.getMessage());
                errors++;
            }
        }

        logger.info("Migration completed. Processed: " + processed + ", Errors: " + errors);
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