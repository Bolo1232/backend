package wildtrack.example.wildtrackbackend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;

@Repository
public interface LibraryRequirementProgressRepository extends JpaRepository<LibraryRequirementProgress, Long> {
    // Find all progress records for a student
    List<LibraryRequirementProgress> findByStudentId(String studentId);

    // Find by student and subject
    List<LibraryRequirementProgress> findByStudentIdAndSubject(String studentId, String subject);

    // Find by student and quarter
    List<LibraryRequirementProgress> findByStudentIdAndQuarter(String studentId, String quarter);

    // Find by student, subject and quarter
    List<LibraryRequirementProgress> findByStudentIdAndSubjectAndQuarter(String studentId, String subject,
            String quarter);

    // Count by student ID
    long countByStudentId(String studentId);

    // Find all completed requirements for a student
    List<LibraryRequirementProgress> findByStudentIdAndIsCompletedTrue(String studentId);

    // Find all in-progress requirements for a student
    List<LibraryRequirementProgress> findByStudentIdAndIsCompletedFalse(String studentId);

    // Find progress for a specific requirement and student
    Optional<LibraryRequirementProgress> findByStudentIdAndRequirementId(String studentId, Long requirementId);

    // Find all requirements for a specific grade level and subject
    List<LibraryRequirementProgress> findByGradeLevelAndSubject(String gradeLevel, String subject);

    // Find all completed requirements
    List<LibraryRequirementProgress> findByIsCompletedTrue();

    // Find all completed requirements by subject
    List<LibraryRequirementProgress> findBySubjectAndIsCompletedTrue(String subject);

    // Find all completed requirements by quarter
    List<LibraryRequirementProgress> findByQuarterAndIsCompletedTrue(String quarter);

    // Find all completed requirements by subject and quarter
    List<LibraryRequirementProgress> findBySubjectAndQuarterAndIsCompletedTrue(String subject, String quarter);

    // Find all completed requirements within a date range
    List<LibraryRequirementProgress> findByIsCompletedTrueAndLastUpdatedBetween(LocalDate startDate, LocalDate endDate);

    // Find all completed requirements by academic year
    List<LibraryRequirementProgress> findByAcademicYearAndIsCompletedTrue(String academicYear);

    // Count completed requirements for a student
    @Query("SELECT COUNT(p) FROM LibraryRequirementProgress p WHERE p.studentId = ?1 AND p.isCompleted = true")
    Integer countCompletedRequirements(String studentId);

    // Count requirements by status
    @Query("SELECT COUNT(p) FROM LibraryRequirementProgress p WHERE p.studentId = ?1 AND p.isCompleted = ?2")
    Integer countRequirementsByStatus(String studentId, boolean isCompleted);

    // Count overdue requirements
    @Query("SELECT COUNT(p) FROM LibraryRequirementProgress p WHERE p.studentId = ?1 AND p.isCompleted = false AND p.deadline < CURRENT_DATE")
    Integer countOverdueRequirements(String studentId);

    // Get sum of minutes rendered for a student
    @Query("SELECT SUM(p.minutesRendered) FROM LibraryRequirementProgress p WHERE p.studentId = ?1")
    Integer getTotalMinutesRendered(String studentId);

    // Get sum of required minutes for a student
    @Query("SELECT SUM(p.requiredMinutes) FROM LibraryRequirementProgress p WHERE p.studentId = ?1")
    Integer getTotalRequiredMinutes(String studentId);

    // Count by last updated date range (for statistics)
    long countByLastUpdatedBetween(LocalDate start, LocalDate end);

    // Count completed requirements by last updated date range
    long countByIsCompletedTrueAndLastUpdatedBetween(LocalDate start, LocalDate end);

    long countByLastUpdatedBetweenAndSubject(LocalDate start, LocalDate end, String subject);

    long countByIsCompletedTrueAndLastUpdatedBetweenAndSubject(LocalDate start, LocalDate end, String subject);

    long countByStudentIdInAndIsCompletedTrueAndLastUpdatedBetween(
            List<String> studentIds, LocalDate startDate, LocalDate endDate);
}