package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
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

    public SetLibraryHours setLibraryHours(SetLibraryHours setLibraryHours) {
        // Set initial approval status to PENDING
        setLibraryHours.setApprovalStatus("PENDING");

        // Save the library hours
        SetLibraryHours savedHours = repository.save(setLibraryHours);

        // Create notification for librarians about pending approval
        notificationService.createLibrarianNotification(
                "New Library Hours Requirement",
                String.format(
                        "A new library hours requirement for %s Grade %s, %s Quarter has been submitted and needs your approval.",
                        savedHours.getSubject(),
                        savedHours.getGradeLevel(),
                        savedHours.getQuarter().getValue()));

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

    public List<SetLibraryHours> getAllSetLibraryHours() {
        return repository.findAll();
    }

    public List<SetLibraryHours> getPendingApprovals() {
        return repository.findByApprovalStatus("PENDING");
    }

    public List<SetLibraryHours> getApprovedRequirements(String gradeLevel) {
        return repository.findByGradeLevelAndApprovalStatus(gradeLevel, "APPROVED");
    }

    public SetLibraryHours approveLibraryHours(Long id) {
        Optional<SetLibraryHours> hoursOpt = repository.findById(id);

        if (hoursOpt.isPresent()) {
            SetLibraryHours hours = hoursOpt.get();
            hours.setApprovalStatus("APPROVED");
            SetLibraryHours approvedHours = repository.save(hours);

            // Notify affected students and the teacher who created the requirement
            notificationService.createLibraryHoursNotification(approvedHours);

            // Get the current user (librarian) ID from security context
            Long librarianId = getCurrentUserId();

            // Log activity for librarian approving library hours
            if (librarianId != null) {
                activityLogService.logLibraryHoursApproval(
                        librarianId,
                        approvedHours.getId(),
                        approvedHours.getSubject(),
                        approvedHours.getGradeLevel(),
                        approvedHours.getQuarter().getValue());
            }

            return approvedHours;
        }

        return null;
    }

    public SetLibraryHours rejectLibraryHours(Long id, String reason) {
        Optional<SetLibraryHours> hoursOpt = repository.findById(id);

        if (hoursOpt.isPresent()) {
            SetLibraryHours hours = hoursOpt.get();
            hours.setApprovalStatus("REJECTED");
            hours.setRejectionReason(reason); // Store the reason in the database
            SetLibraryHours rejectedHours = repository.save(hours);

            // Check if we know who created this requirement
            if (hours.getCreatedById() != null) {
                // Notify only the teacher who created it
                notificationService.createUserNotification(
                        hours.getCreatedById(),
                        "Library Hours Requirement Rejected",
                        String.format(
                                "Your library hours requirement for %s Grade %s, %s Quarter was rejected. Reason: %s",
                                hours.getSubject(),
                                hours.getGradeLevel(),
                                hours.getQuarter().getValue(),
                                reason),
                        "LIBRARY_HOURS_REJECTED",
                        hours.getId());
            } else {
                // Fallback if creator is unknown - notify all teachers
                notificationService.createTeacherNotification(
                        "Library Hours Requirement Rejected",
                        String.format(
                                "A library hours requirement for %s Grade %s, %s Quarter was rejected. Reason: %s",
                                hours.getSubject(),
                                hours.getGradeLevel(),
                                hours.getQuarter().getValue(),
                                reason));
            }

            // Get the current user (librarian) ID from security context
            Long librarianId = getCurrentUserId();

            // Log activity for librarian rejecting library hours
            if (librarianId != null) {
                activityLogService.logLibraryHoursRejection(
                        librarianId,
                        rejectedHours.getId(),
                        rejectedHours.getSubject(),
                        rejectedHours.getGradeLevel(),
                        rejectedHours.getQuarter().getValue(),
                        reason);
            }

            return rejectedHours;
        }

        return null;
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