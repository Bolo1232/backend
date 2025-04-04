package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import java.util.List;

@Repository
public interface SetLibraryHoursRepository extends JpaRepository<SetLibraryHours, Long> {
    List<SetLibraryHours> findByGradeLevel(String gradeLevel);

    // Methods to find by approval status
    List<SetLibraryHours> findByApprovalStatus(String approvalStatus);

    // Find approved requirements for a grade level
    List<SetLibraryHours> findByGradeLevelAndApprovalStatus(String gradeLevel, String approvalStatus);

    // Find requirements by subject and grade level
    List<SetLibraryHours> findBySubjectAndGradeLevel(String subject, String gradeLevel);
}