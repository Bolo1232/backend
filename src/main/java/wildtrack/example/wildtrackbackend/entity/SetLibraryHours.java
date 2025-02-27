package wildtrack.example.wildtrackbackend.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;

@Entity
public class SetLibraryHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer minutes;

    private String gradeLevel;

    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Quarter quarter;

    private LocalDate deadline;

    // New field for approval status
    @Column(nullable = false)
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    // Fields to handle date input
    @Transient
    private Integer month;

    @Transient
    private Integer day;

    @Transient
    private Integer year;

    // Enum for Quarter with better JSON serialization
    public enum Quarter {
        First("First"),
        Second("Second"),
        Third("Third"),
        Fourth("Fourth");

        private final String value;

        Quarter(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public static Quarter fromString(String text) {
            for (Quarter q : Quarter.values()) {
                if (q.value.equalsIgnoreCase(text)) {
                    return q;
                }
            }
            throw new IllegalArgumentException("No quarter found with value: " + text);
        }
    }

    // Default constructor
    public SetLibraryHours() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public String getSubject() {
        return subject;
    }

    public Quarter getQuarter() {
        return quarter;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getDay() {
        return day;
    }

    public Integer getYear() {
        return year;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setQuarter(Quarter quarter) {
        this.quarter = quarter;
    }

    // Additional method to handle string-based quarter setting
    public void setQuarter(String quarterStr) {
        this.quarter = Quarter.fromString(quarterStr);
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    // Method to construct deadline from month, day, year
    @PrePersist
    @PreUpdate
    public void constructDeadline() {
        if (month != null && day != null && year != null) {
            this.deadline = LocalDate.of(year, month, day);
        }
    }

    @Override
    public String toString() {
        return "SetLibraryHours{" +
                "id=" + id +
                ", minutes=" + minutes +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", subject='" + subject + '\'' +
                ", quarter=" + quarter.getValue() +
                ", deadline=" + deadline +
                ", approvalStatus='" + approvalStatus + '\'' +
                '}';
    }
}