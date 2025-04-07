package wildtrack.example.wildtrackbackend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_id_number")
    private String userIdNumber; // This will store the display ID like "129"

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "activity")
    private String activity;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "academic_year")
    private String academicYear;

    // Default constructor
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with fields
    public ActivityLog(Long userId, String userIdNumber, String userName, String userRole,
            String activity, String description, String academicYear) {
        this.userId = userId;
        this.userIdNumber = userIdNumber; // Store the user's display ID from User.idNumber
        this.userName = userName;
        this.userRole = userRole;
        this.timestamp = LocalDateTime.now();
        this.activity = activity;
        this.description = description;
        this.academicYear = academicYear;
    }

    // Getters and Setters
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

    public String getUserIdNumber() {
        return userIdNumber;
    }

    public void setUserIdNumber(String userIdNumber) {
        this.userIdNumber = userIdNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    @Override
    public String toString() {
        return "ActivityLog [id=" + id + ", userId=" + userId + ", userIdNumber=" + userIdNumber
                + ", userName=" + userName + ", userRole=" + userRole
                + ", timestamp=" + timestamp + ", activity=" + activity + ", description=" + description
                + ", academicYear=" + academicYear + "]";
    }
}