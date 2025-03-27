package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.Notification;
import wildtrack.example.wildtrackbackend.service.NotificationService;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")

public class PasswordResetController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, Object> requestBody) {
        try {
            Long userId = ((Number) requestBody.get("userId")).longValue();
            String idNumber = (String) requestBody.get("idNumber");
            String userName = (String) requestBody.get("userName");

            // Create a notification for all librarians
            String title = "Password Reset Request";
            String message = String.format(
                    "User %s (ID: %s) has requested a password reset. Please reset their password using the 'Reset Password' function in the User Management section.",
                    userName, idNumber);

            Notification notification = notificationService.createLibrarianNotification(
                    title,
                    message);

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset request sent to librarian",
                    "notificationId", notification.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to process password reset request: " + e.getMessage()));
        }
    }
}