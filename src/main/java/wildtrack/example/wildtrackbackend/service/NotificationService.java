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

    // Create notifications for an approved library hours requirement
    public List<Notification> createLibraryHoursNotification(SetLibraryHours libraryHours) {
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

        // Find all students in the specified grade level
        List<User> studentsInGrade = userRepository.findByRoleAndGrade("Student", libraryHours.getGradeLevel());

        // Create individual notifications for each student
        for (User student : studentsInGrade) {
            Notification notification = new Notification(
                    student.getId(), // Individual student ID
                    title,
                    message,
                    "LIBRARY_HOURS",
                    libraryHours.getId());

            notificationRepository.save(notification);
        }

        // Also create a notification for the teacher who created the requirement
        if (libraryHours.getCreatedById() != null) {
            String teacherMessage = String.format(
                    "Your library hours requirement for %s: %d minutes of %s reading due by %s (Quarter %s) has been created.",
                    libraryHours.getGradeLevel(),
                    libraryHours.getMinutes(),
                    libraryHours.getSubject(),
                    formattedDate,
                    libraryHours.getQuarter().getValue());

            Notification teacherNotification = new Notification(
                    libraryHours.getCreatedById(),
                    "Library Hours Requirement Created",
                    teacherMessage,
                    "LIBRARY_HOURS_APPROVED",
                    libraryHours.getId());

            notificationRepository.save(teacherNotification);
        }

        // Return all created notifications
        return notificationRepository.findByReferenceId(libraryHours.getId());
    }

    // Create notifications for students when a library hours requirement is updated
    public List<Notification> createLibraryHoursUpdateNotification(SetLibraryHours libraryHours) {
        // Only create notifications for approved library hours
        if (!"APPROVED".equals(libraryHours.getApprovalStatus())) {
            return null;
        }

        String formattedDate = libraryHours.getDeadline() != null
                ? libraryHours.getDeadline().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                : "No deadline";

        String title = "Library Requirement Updated";
        String message = String.format(
                "The %s reading requirement for %s Quarter has been updated: %d minutes due by %s",
                libraryHours.getSubject(),
                libraryHours.getQuarter().getValue(),
                libraryHours.getMinutes(),
                formattedDate);

        if (libraryHours.getTask() != null && !libraryHours.getTask().isEmpty()) {
            message += String.format("\n\nTask: %s", libraryHours.getTask());
        }

        // Find all students in the specified grade level
        List<User> studentsInGrade = userRepository.findByRoleAndGrade("Student", libraryHours.getGradeLevel());

        // Create individual notifications for each student
        for (User student : studentsInGrade) {
            Notification notification = new Notification(
                    student.getId(),
                    title,
                    message,
                    "LIBRARY_HOURS_UPDATED",
                    libraryHours.getId());

            notificationRepository.save(notification);
        }

        // Also create a notification for the teacher who created the requirement (if
        // different from updater)
        if (libraryHours.getCreatedById() != null) {
            String teacherMessage = String.format(
                    "Your library hours requirement for %s: %d minutes of %s reading (Quarter %s) has been updated. New deadline: %s",
                    libraryHours.getGradeLevel(),
                    libraryHours.getMinutes(),
                    libraryHours.getSubject(),
                    libraryHours.getQuarter().getValue(),
                    formattedDate);

            Notification teacherNotification = new Notification(
                    libraryHours.getCreatedById(),
                    "Library Hours Requirement Updated",
                    teacherMessage,
                    "LIBRARY_HOURS_UPDATED",
                    libraryHours.getId());

            notificationRepository.save(teacherNotification);
        }

        // Return all created notifications
        return notificationRepository.findByReferenceId(libraryHours.getId());
    }

    // Create notification when library hours are rejected
    public Notification createLibraryHoursRejectionNotification(SetLibraryHours libraryHours, String reason) {
        // Find the teacher who created the library hours
        Long createdById = libraryHours.getCreatedById();
        String formattedDate = libraryHours.getDeadline().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        if (createdById == null) {
            // If we don't have a specific teacher ID, notify all teachers
            return createTeacherNotification(
                    "Library Hours Requirement Rejected",
                    String.format(
                            "The library hours requirement for %s: %d minutes of %s reading due by %s (Quarter %s) was rejected. Reason: %s",
                            libraryHours.getGradeLevel(),
                            libraryHours.getMinutes(),
                            libraryHours.getSubject(),
                            formattedDate,
                            libraryHours.getQuarter().getValue(),
                            reason));
        }

        // Create a notification for the specific teacher
        String title = "Library Hours Requirement Rejected";
        String message = String.format(
                "Your library hours requirement for %s: %d minutes of %s reading due by %s (Quarter %s) was rejected.\n\nReason: %s",
                libraryHours.getGradeLevel(),
                libraryHours.getMinutes(),
                libraryHours.getSubject(),
                formattedDate,
                libraryHours.getQuarter().getValue(),
                reason);

        Notification notification = new Notification(
                createdById,
                title,
                message,
                "LIBRARY_HOURS_REJECTED",
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
        // Only get notifications specifically for this user ID
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Get notifications for a user by ID number
    public List<Notification> getUserNotificationsByIdNumber(String idNumber) {
        Optional<User> userOpt = userRepository.findByIdNumber(idNumber);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
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
    public void markAllAsRead(Long userId) { // removed unused gradeLevel parameter
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Get unread notification count
    public Long getUnreadCount(Long userId) { // removed unused gradeLevel parameter
        return notificationRepository.countByUserIdAndIsReadFalse(userId); // Fixed method name
    }

    // Create notification for individual user
    public Notification createUserNotification(Long userId, String title, String message, String type,
            Long referenceId) {
        Notification notification = new Notification(userId, title, message, type, referenceId);
        return notificationRepository.save(notification);
    }

    // Delete notification
    public void deleteNotification(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            notificationRepository.deleteById(notificationId);
        } else {
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }
    }
}