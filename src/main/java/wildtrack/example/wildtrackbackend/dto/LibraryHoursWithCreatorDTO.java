package wildtrack.example.wildtrackbackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.SetLibraryHours.Quarter;

public class LibraryHoursWithCreatorDTO {
    private Long id;
    private Integer minutes;
    private String gradeLevel;
    private String subject;
    private String task;
    private String quarter;
    private LocalDate deadline;
    private Long createdById;
    private LocalDateTime createdAt;
    private String creatorName;
    private String creatorRole;

    // Default constructor
    public LibraryHoursWithCreatorDTO() {
    }

    // Constructor with SetLibraryHours entity
    public LibraryHoursWithCreatorDTO(SetLibraryHours libraryHours) {
        this.id = libraryHours.getId();
        this.minutes = libraryHours.getMinutes();
        this.gradeLevel = libraryHours.getGradeLevel();
        this.subject = libraryHours.getSubject();
        this.task = libraryHours.getTask();
        this.quarter = libraryHours.getQuarter() != null ? libraryHours.getQuarter().getValue() : null;
        this.deadline = libraryHours.getDeadline();
        this.createdById = libraryHours.getCreatedById();
        this.createdAt = libraryHours.getCreatedAt();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorRole() {
        return creatorRole;
    }

    public void setCreatorRole(String creatorRole) {
        this.creatorRole = creatorRole;
    }
}