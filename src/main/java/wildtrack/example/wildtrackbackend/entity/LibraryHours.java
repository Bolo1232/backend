package wildtrack.example.wildtrackbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "library_hours")
public class LibraryHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "time_in", nullable = false)
    private LocalDateTime timeIn;

    @Column(name = "book_title")
    private String bookTitle;

    @Column(name = "subject", nullable = true)
    private String subject;

    // Field declaration
    @Column(name = "minutes_counted")
    private Integer minutesCounted;

    // Field declarations
    @Column(name = "requirement_id")
    private Long requirementId;

    @Column(name = "is_counted", nullable = false)
    private Boolean isCounted = false;

    @Column(name = "summary", length = 1000)
    private String summary;

    @Column(name = "requires_book_assignment")
    private Boolean requiresBookAssignment = false;

    // Add getter and setter
    public Boolean getRequiresBookAssignment() {
        return requiresBookAssignment;
    }

    public void setRequiresBookAssignment(Boolean requiresBookAssignment) {
        this.requiresBookAssignment = requiresBookAssignment;
    }

    // Getter and setter for summary
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    // Getter and setter for requirementId
    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    // Getter and setter for isCounted
    public Boolean getIsCounted() {
        return isCounted;
    }

    public void setIsCounted(Boolean isCounted) {
        this.isCounted = isCounted;
    }

    // Getter method
    public Integer getMinutesCounted() {
        return minutesCounted;
    }

    // Setter method
    public void setMinutesCounted(Integer minutesCounted) {
        this.minutesCounted = minutesCounted;
    }

    // Add getter and setter
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    private LocalDateTime timeOut;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public LocalDateTime getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(LocalDateTime timeIn) {
        this.timeIn = timeIn;
    }

    public LocalDateTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalDateTime timeOut) {
        this.timeOut = timeOut;
    }
}