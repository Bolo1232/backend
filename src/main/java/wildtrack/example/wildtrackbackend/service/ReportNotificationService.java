package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wildtrack.example.wildtrackbackend.entity.Notification;
import wildtrack.example.wildtrackbackend.entity.Report;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

import java.util.List;

@Service
public class ReportNotificationService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a notification for librarians when a report is submitted
     * 
     * @param report The submitted report
     * @return The created notification
     */
    public Notification createReportSubmissionNotification(Report report) {
        // Find all librarian users
        List<User> librarians = userRepository.findByRole("Librarian");

        if (librarians.isEmpty()) {
            // If no librarians, create a system notification
            return notificationService.createLibrarianNotification(
                    "New Report Submitted",
                    String.format("A new report has been submitted by %s (%s).\n\nIssue: %s",
                            report.getUserName(),
                            report.getRole(),
                            report.getIssue()));
        }

        // Create notification for each librarian
        Notification firstNotification = null;
        String title = "New Report Submitted";
        String message = String.format(
                "A new report has been submitted by %s (%s).\n\nIssue: %s\n\nPlease review and address this report.",
                report.getUserName(),
                report.getRole(),
                report.getIssue());

        for (User librarian : librarians) {
            Notification notification = new Notification(
                    librarian.getId(),
                    title,
                    message,
                    "REPORT_SUBMISSION",
                    report.getId());

            Notification saved = notificationService.createUserNotification(
                    librarian.getId(),
                    title,
                    message,
                    "REPORT_SUBMISSION",
                    report.getId());

            if (firstNotification == null) {
                firstNotification = saved;
            }
        }

        return firstNotification;
    }

    /**
     * Creates a notification for the report submitter when their report is resolved
     * 
     * @param report The resolved report
     * @return The created notification
     */
    public Notification createReportResolutionNotification(Report report) {
        // Only notify if we have a user ID
        if (report.getUserId() == null) {
            return null;
        }

        String title = "Your Report Has Been Resolved";
        String message = String.format(
                "Your report '%s' has been resolved by library staff.\n\n",
                report.getIssue());

        if (report.getAdminComments() != null && !report.getAdminComments().isEmpty()) {
            message += "Administrator comments: " + report.getAdminComments();
        }

        return notificationService.createUserNotification(
                report.getUserId(),
                title,
                message,
                "REPORT_RESOLVED",
                report.getId());
    }
}