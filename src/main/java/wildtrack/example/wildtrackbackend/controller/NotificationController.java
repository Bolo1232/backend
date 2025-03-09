package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.Notification;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.NotificationService;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // Get all notifications for a user by ID number
    @GetMapping("/user/{idNumber}")
    public ResponseEntity<?> getUserNotifications(@PathVariable String idNumber) {
        try {
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found with ID number: " + idNumber));
            }

            List<Notification> notifications = notificationService.getUserNotifications(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving notifications: " + e.getMessage()));
        }
    }

    // Get unread notification count
    @GetMapping("/unread-count/{idNumber}")
    public ResponseEntity<?> getUnreadCount(@PathVariable String idNumber) {
        try {
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found with ID number: " + idNumber));
            }

            Long unreadCount = notificationService.getUnreadCount(user.getId()); // Remove the second parameter
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving unread count: " + e.getMessage()));
        }
    }

    // Mark notification as read
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            Notification notification = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error marking notification as read: " + e.getMessage()));
        }
    }

    // Mark all notifications as read for a user
    // In NotificationController.java
    @PutMapping("/mark-all-read/{idNumber}")
    public ResponseEntity<?> markAllAsRead(@PathVariable String idNumber) {
        try {
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found with ID number: " + idNumber));
            }

            notificationService.markAllAsRead(user.getId()); // Remove the second parameter
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error marking all notifications as read: " + e.getMessage()));
        }
    }

    // Delete notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting notification: " + e.getMessage()));
        }
    }
}