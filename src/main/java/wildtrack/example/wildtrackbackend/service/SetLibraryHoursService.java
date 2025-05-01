package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.dto.LibraryHoursWithCreatorDTO;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.SetLibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@Service
public class SetLibraryHoursService {

    @Autowired
    private SetLibraryHoursRepository repository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    public SetLibraryHours setLibraryHours(SetLibraryHours setLibraryHours) {
        // Save the library hours requirement
        SetLibraryHours savedHours = repository.save(setLibraryHours);

        // Create notification for students about the new library hours requirement
        notificationService.createLibraryHoursNotification(savedHours);

        // Log activity for teacher creating library hours
        if (savedHours.getCreatedById() != null) {
            activityLogService.logLibraryHoursCreation(
                    savedHours.getCreatedById(),
                    savedHours.getSubject(),
                    savedHours.getGradeLevel(),
                    savedHours.getQuarter().getValue());
        }

        return savedHours;
    }

    /**
     * Update a library hours requirement and sync changes to all student progress
     * records
     */
    @Transactional
    public SetLibraryHours updateLibraryHours(Long id, SetLibraryHours updatedHours) {
        // Find the existing requirement
        SetLibraryHours existingHours = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library hours requirement not found with id: " + id));

        System.out.println("Updating library hours requirement: " + id);
        System.out.println("Original values: Minutes=" + existingHours.getMinutes() +
                ", Quarter=" + existingHours.getQuarter().getValue() +
                ", Deadline=" + existingHours.getDeadline());

        // Log incoming request data for debugging
        System.out.println("New values: Minutes=" + updatedHours.getMinutes() +
                ", Quarter=" + (updatedHours.getQuarter() != null ? updatedHours.getQuarter().getValue() : "null") +
                ", Deadline=" + updatedHours.getDeadline());
        System.out.println("Date parts: Year=" + updatedHours.getYear() +
                ", Month=" + updatedHours.getMonth() +
                ", Day=" + updatedHours.getDay());

        // Only update fields that are not null in the request
        if (updatedHours.getMinutes() != null) {
            existingHours.setMinutes(updatedHours.getMinutes());
        }

        if (updatedHours.getGradeLevel() != null) {
            existingHours.setGradeLevel(updatedHours.getGradeLevel());
        }

        if (updatedHours.getSubject() != null) {
            existingHours.setSubject(updatedHours.getSubject());
        }

        if (updatedHours.getQuarter() != null) {
            existingHours.setQuarter(updatedHours.getQuarter());
        }

        if (updatedHours.getTask() != null) {
            existingHours.setTask(updatedHours.getTask());
        }

        // Update deadline - handle both direct deadline update and date parts
        boolean datePartsProvided = updatedHours.getMonth() != null &&
                updatedHours.getDay() != null &&
                updatedHours.getYear() != null;

        if (datePartsProvided) {
            System.out.println("Updating deadline using date parts: " +
                    updatedHours.getYear() + "-" +
                    updatedHours.getMonth() + "-" +
                    updatedHours.getDay());

            existingHours.setMonth(updatedHours.getMonth());
            existingHours.setDay(updatedHours.getDay());
            existingHours.setYear(updatedHours.getYear());

            // Force deadline update instead of relying just on @PreUpdate
            try {
                LocalDate newDate = LocalDate.of(
                        updatedHours.getYear(),
                        updatedHours.getMonth(),
                        updatedHours.getDay());
                existingHours.setDeadline(newDate);
                System.out.println("Deadline explicitly set to: " + newDate);
            } catch (Exception e) {
                System.out.println("Error creating date from parts: " + e.getMessage());
            }
        } else if (updatedHours.getDeadline() != null) {
            existingHours.setDeadline(updatedHours.getDeadline());
            System.out.println("Deadline directly set to: " + updatedHours.getDeadline());
        }

        // Save the updated requirement
        SetLibraryHours savedHours = repository.save(existingHours);
        System.out.println("After update, deadline is: " + savedHours.getDeadline());

        // Now, update all related student progress records
        updateStudentProgressRecords(savedHours);

        // Optional: Create notification about the updated requirement
        notificationService.createLibraryHoursUpdateNotification(savedHours);

        // Log the activity
        if (savedHours.getCreatedById() != null) {
            activityLogService.logLibraryHoursUpdate(
                    savedHours.getCreatedById(),
                    savedHours.getSubject(),
                    savedHours.getGradeLevel(),
                    savedHours.getQuarter().getValue());
        }

        return savedHours;
    }

    /**
     * Update all student progress records related to this library hours requirement
     */
    private void updateStudentProgressRecords(SetLibraryHours libraryHours) {
        // Find all progress records for this requirement
        List<LibraryRequirementProgress> progressRecords = progressRepository.findByRequirementId(libraryHours.getId());

        System.out.println("Found " + progressRecords.size() + " progress records to update for requirement ID: "
                + libraryHours.getId());

        // Update each progress record with the new requirement details
        for (LibraryRequirementProgress progress : progressRecords) {
            // Store original values for logging
            int originalRequired = progress.getRequiredMinutes();
            String originalQuarter = progress.getQuarter();

            // Update the progress record fields
            progress.setSubject(libraryHours.getSubject());
            progress.setQuarter(libraryHours.getQuarter().getValue());
            progress.setRequiredMinutes(libraryHours.getMinutes());
            progress.setDeadline(libraryHours.getDeadline());

            // Check if completion status needs updating
            if (progress.getMinutesRendered() >= libraryHours.getMinutes() && !progress.getIsCompleted()) {
                progress.setIsCompleted(true);
            } else if (progress.getMinutesRendered() < libraryHours.getMinutes() && progress.getIsCompleted()) {
                progress.setIsCompleted(false);
            }

            // Log the changes
            System.out.println("Updating progress record ID: " + progress.getId() +
                    ", Student ID: " + progress.getStudentId() +
                    ", Minutes: " + originalRequired + " -> " + libraryHours.getMinutes() +
                    ", Quarter: " + originalQuarter + " -> " + libraryHours.getQuarter().getValue());

            // Save the updated progress record
            progressRepository.save(progress);
        }
    }

    public List<SetLibraryHours> getAllSetLibraryHours() {
        return repository.findAll();
    }

    // Get all library hours with creator details
    public List<LibraryHoursWithCreatorDTO> getAllLibraryHoursWithCreator() {
        List<SetLibraryHours> allHours = repository.findAll();

        return allHours.stream().map(hours -> {
            LibraryHoursWithCreatorDTO dto = new LibraryHoursWithCreatorDTO(hours);

            // Fetch creator details if createdById exists
            if (hours.getCreatedById() != null) {
                Optional<User> creatorOpt = userRepository.findById(hours.getCreatedById());
                if (creatorOpt.isPresent()) {
                    User creator = creatorOpt.get();
                    String firstName = creator.getFirstName() != null ? creator.getFirstName() : "";
                    String lastName = creator.getLastName() != null ? creator.getLastName() : "";
                    String fullName = (firstName + " " + lastName).trim();

                    if (!fullName.isEmpty()) {
                        dto.setCreatorName(fullName);
                    } else if (creator.getIdNumber() != null) {
                        dto.setCreatorName("Teacher ID: " + creator.getIdNumber());
                    } else {
                        dto.setCreatorName("Teacher #" + creator.getId());
                    }

                    dto.setCreatorRole(creator.getRole());
                } else {
                    dto.setCreatorName("Unknown");
                    dto.setCreatorRole("Unknown");
                }
            } else {
                dto.setCreatorName("Unknown");
                dto.setCreatorRole("Unknown");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // Get library hours requirements for a specific grade level
    public List<SetLibraryHours> getRequirementsForGradeLevel(String gradeLevel) {
        return repository.findByGradeLevel(gradeLevel);
    }

    // Get all subjects for a grade level
    public List<String> getSubjectsForGrade(String gradeLevel) {
        List<SetLibraryHours> hoursForGrade = repository.findByGradeLevel(gradeLevel);
        return hoursForGrade.stream()
                .map(SetLibraryHours::getSubject)
                .distinct()
                .collect(Collectors.toList());
    }

    // Get a specific library hours requirement by ID
    public SetLibraryHours getLibraryHoursById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library hours requirement not found with id: " + id));
    }

    // Helper method to get current user ID from security context
    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName(); // This is idNumber

                // Find user by ID number
                Optional<User> userOpt = userRepository.findByIdNumber(username);
                return userOpt.map(User::getId).orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}