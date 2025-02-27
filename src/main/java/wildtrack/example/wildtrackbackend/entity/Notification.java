package wildtrack.example.wildtrackbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @Column(name = "grade_level", nullable = true)
    private String gradeLevel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", length = 1000, nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "reference_id", nullable = true)
    private Long referenceId;

    // Default constructor
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Constructor for grade-level notifications (no specific userId)
    public Notification(String gradeLevel, String title, String message, String notificationType, Long referenceId) {
        this();
        this.gradeLevel = gradeLevel;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.referenceId = referenceId;
    }

    // Constructor for user-specific notifications
    public Notification(Long userId, String title, String message, String notificationType, Long referenceId) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.referenceId = referenceId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}