package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.repository.SetLibraryHoursRepository;

@Service
public class SetLibraryHoursService {

    @Autowired
    private SetLibraryHoursRepository repository;

    @Autowired
    private NotificationService notificationService;

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
            repository.save(hours);

            // Notify affected students
            notificationService.createLibraryHoursNotification(hours);

            return hours;
        }

        return null;
    }

    public SetLibraryHours rejectLibraryHours(Long id, String reason) {
        Optional<SetLibraryHours> hoursOpt = repository.findById(id);

        if (hoursOpt.isPresent()) {
            SetLibraryHours hours = hoursOpt.get();
            hours.setApprovalStatus("REJECTED");
            repository.save(hours);

            // Notify the teacher
            notificationService.createTeacherNotification(
                    "Library Hours Requirement Rejected",
                    String.format("Your library hours requirement for %s Grade %s, %s Quarter was rejected. Reason: %s",
                            hours.getSubject(),
                            hours.getGradeLevel(),
                            hours.getQuarter().getValue(),
                            reason));

            return hours;
        }

        return null;
    }
}