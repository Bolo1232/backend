package wildtrack.example.wildtrackbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "library_requirement_progress")
public class LibraryRequirementProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "quarter", nullable = false)
    private String quarter;

    @Column(name = "grade_level", nullable = false)
    private String gradeLevel;

    @Column(name = "required_minutes", nullable = false)
    private Integer requiredMinutes;

    @Column(name = "minutes_rendered", nullable = false)
    private Integer minutesRendered = 0;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "last_updated")
    private LocalDate lastUpdated;

    // Default constructor
    public LibraryRequirementProgress() {
        this.lastUpdated = LocalDate.now();
    }

    // Constructors with fields
    public LibraryRequirementProgress(String studentId, Long requirementId, String subject,
            String quarter, String gradeLevel, Integer requiredMinutes,
            LocalDate deadline) {
        this.studentId = studentId;
        this.requirementId = requirementId;
        this.subject = subject;
        this.quarter = quarter;
        this.gradeLevel = gradeLevel;
        this.requiredMinutes = requiredMinutes;
        this.minutesRendered = 0;
        this.deadline = deadline;
        this.isCompleted = false;
        this.lastUpdated = LocalDate.now();
    }

    // Add minutes rendered and automatically complete if threshold is met
    public void addMinutes(int minutes) {
        this.minutesRendered += minutes;
        this.lastUpdated = LocalDate.now();

        // Automatically mark as completed when required minutes are reached
        if (this.minutesRendered >= this.requiredMinutes && !this.isCompleted) {
            this.isCompleted = true;
        }
    }

    // Calculate percentage
    public double getProgressPercentage() {
        if (requiredMinutes == 0)
            return 0;
        double percentage = ((double) minutesRendered / requiredMinutes) * 100;
        return Math.min(100.0, percentage);
    }

    // Calculate remaining minutes
    public int getRemainingMinutes() {
        return Math.max(0, requiredMinutes - minutesRendered);
    }

    // Determine status - simplified without approval status
    public String getStatus() {
        if (isCompleted) {
            return "Completed";
        } else if (deadline != null && deadline.isBefore(LocalDate.now())) {
            return "Overdue";
        } else {
            return "In Progress";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public Integer getRequiredMinutes() {
        return requiredMinutes;
    }

    public void setRequiredMinutes(Integer requiredMinutes) {
        this.requiredMinutes = requiredMinutes;
    }

    public Integer getMinutesRendered() {
        return minutesRendered;
    }

    public void setMinutesRendered(Integer minutesRendered) {
        this.minutesRendered = minutesRendered;

        // Automatically complete when required minutes are reached
        if (this.minutesRendered >= this.requiredMinutes && !this.isCompleted) {
            this.isCompleted = true;
        }

        this.lastUpdated = LocalDate.now();
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}