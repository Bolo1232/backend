package wildtrack.example.wildtrackbackend.service;

import wildtrack.example.wildtrackbackend.entity.Notification;
import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.NotificationRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a notification for a library hours deadline
    public Notification createLibraryHoursNotification(SetLibraryHours libraryHours) {
        // Only create notifications for approved library hours
        if (!"APPROVED".equals(libraryHours.getApprovalStatus())) {
            return null;
        }

        String formattedDate = libraryHours.getDeadline().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String title = "New Library Hours Requirement";
        String message = String.format(
                "A new library hours requirement has been set for %s: %d minutes of %s reading due by %s (Quarter %s)",
                libraryHours.getGradeLevel(),
                libraryHours.getMinutes(),
                libraryHours.getSubject(),
                formattedDate,
                libraryHours.getQuarter().getValue());

        Notification notification = new Notification(
                libraryHours.getGradeLevel(),
                title,
                message,
                "LIBRARY_HOURS",
                libraryHours.getId());

        return notificationRepository.save(notification);
    }

    // Create notification for librarians
    public Notification createLibrarianNotification(String title, String message) {
        // Find all librarian users
        List<User> librarians = userRepository.findByRole("Librarian");

        if (librarians.isEmpty()) {
            // If no librarians, create a global notification
            Notification notification = new Notification((String) null, title, message, "LIBRARIAN_ALERT", null);
            return notificationRepository.save(notification);
        }

        // Create a notification for each librarian
        Notification firstNotification = null;
        for (User librarian : librarians) {
            Notification notification = new Notification(
                    librarian.getId(),
                    title,
                    message,
                    "LIBRARIAN_ALERT",
                    null);
            Notification saved = notificationRepository.save(notification);

            if (firstNotification == null) {
                firstNotification = saved;
            }
        }

        return firstNotification;
    }

    // Create notification for teachers
    public Notification createTeacherNotification(String title, String message) {
        // Find all teacher users
        List<User> teachers = userRepository.findByRole("Teacher");

        if (teachers.isEmpty()) {
            // If no teachers, create a global notification
            Notification notification = new Notification((String) null, title, message, "TEACHER_ALERT", null);
            return notificationRepository.save(notification);
        }

        // Create a notification for each teacher
        Notification firstNotification = null;
        for (User teacher : teachers) {
            Notification notification = new Notification(
                    teacher.getId(),
                    title,
                    message,
                    "TEACHER_ALERT",
                    null);
            Notification saved = notificationRepository.save(notification);

            if (firstNotification == null) {
                firstNotification = saved;
            }
        }

        return firstNotification;
    }

    // Get notifications for a specific user
    public List<Notification> getUserNotifications(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return notificationRepository.findByUserIdOrGradeLevel(userId, user.getGrade());
        } else {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
    }

    // Get notifications for a user by ID number
    public List<Notification> getUserNotificationsByIdNumber(String idNumber) {
        Optional<User> userOpt = userRepository.findByIdNumber(idNumber);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return notificationRepository.findByUserIdOrGradeLevel(user.getId(), user.getGrade());
        }
        return List.of(); // Return empty list if user not found
    }

    // Mark notification as read
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        throw new RuntimeException("Notification not found with ID: " + notificationId);
    }

    // Mark all notifications as read for a user
    public void markAllAsRead(Long userId, String gradeLevel) {
        List<Notification> notifications = notificationRepository.findByUserIdOrGradeLevel(userId, gradeLevel);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Get unread notification count
    public Long getUnreadCount(Long userId, String gradeLevel) {
        return notificationRepository.countUnreadNotifications(userId, gradeLevel);
    }

    // Create notification for individual user
    public Notification createUserNotification(Long userId, String title, String message, String type,
            Long referenceId) {
        Notification notification = new Notification(userId, title, message, type, referenceId);
        return notificationRepository.save(notification);
    }
}