package wildtrack.example.wildtrackbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Entity
@Table(name = "users")
public class User {
    private static final Logger logger = Logger.getLogger(User.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Column(name = "role", nullable = false)
    private String role;

    // User identification
    @Column(name = "id_number", nullable = false, unique = true)
    @NotBlank(message = "ID Number is required")
    private String idNumber;

    @Column(name = "grade", nullable = true)
    private String grade;

    // New field to track when grade was last updated
    @Column(name = "grade_updated_at")
    private LocalDateTime gradeUpdatedAt;

    @Column(name = "section", nullable = true)
    private String section;

    // New fields: Quarter and Subject
    @Column(name = "quarter", nullable = true)
    private String quarter;

    @Column(name = "subject", nullable = true)
    private String subject;

    @Column(name = "work_period", nullable = true)
    private String workPeriod;

    @Column(name = "assigned_task", nullable = true)
    private String assignedTask;

    @Transient
    private String currentPassword;

    @Column(name = "academic_year", nullable = true)
    private String academicYear;

    @Column(name = "middle_name", nullable = true)
    private String middleName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "position", nullable = true)
    private String position;

    @Column(name = "department", nullable = true)
    private String department;

    @Column(name = "password_reset_required", nullable = false)
    private boolean passwordResetRequired = false;

    // Add created_at column to track when user was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor should initialize createdAt
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter and setter for gradeUpdatedAt
    public LocalDateTime getGradeUpdatedAt() {
        return gradeUpdatedAt;
    }

    public void setGradeUpdatedAt(LocalDateTime gradeUpdatedAt) {
        this.gradeUpdatedAt = gradeUpdatedAt;
    }

    // Add getter and setter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Add getters and setters
    public boolean isPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }

    // Add getters and setters
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    // Add getter and setter for middle name
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    // Add getter and setter
    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    // Getters and setters for the new fields

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(String workPeriod) {
        this.workPeriod = workPeriod;
    }

    public String getAssignedTask() {
        return assignedTask;
    }

    public void setAssignedTask(String assignedTask) {
        this.assignedTask = assignedTask;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // Getters and setters for existing fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getGrade() {
        return grade;
    }

    // Modified setGrade method to track grade changes
    public void setGrade(String grade) {
        // Only update the timestamp if grade is actually changing
        if (this.grade != null && !this.grade.equals(grade)) {
            this.gradeUpdatedAt = LocalDateTime.now();
            logger.info("Grade changed from " + this.grade + " to " + grade + " for user " + this.idNumber);
        } else if (this.grade == null && grade != null) {
            // First time grade is being set
            this.gradeUpdatedAt = LocalDateTime.now();
            logger.info("Grade initially set to " + grade + " for user " + this.idNumber);
        }

        this.grade = grade;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder(firstName);

        // Add middle initial if middle name exists
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ")
                    .append(middleName.charAt(0))
                    .append(".");
        }

        // Add last name
        fullName.append(" ").append(lastName);

        return fullName.toString();
    }
}